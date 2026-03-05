package fr.lachemoilagrappe.domain.usecase

import fr.lachemoilagrappe.data.local.db.entity.SpamEntry
import fr.lachemoilagrappe.domain.model.CallAction
import fr.lachemoilagrappe.domain.repository.ContactsRepository
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import fr.lachemoilagrappe.domain.repository.SpamRepository
import fr.lachemoilagrappe.domain.repository.UserListRepository
import fr.lachemoilagrappe.util.PhoneNumberHelper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DecideCallActionUseCaseTest {

    private lateinit var contactsRepository: ContactsRepository
    private lateinit var spamRepository: SpamRepository
    private lateinit var userListRepository: UserListRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var phoneNumberHelper: PhoneNumberHelper
    private lateinit var useCase: DecideCallActionUseCase

    @Before
    fun setup() {
        contactsRepository = mockk()
        spamRepository = mockk()
        userListRepository = mockk()
        settingsRepository = mockk()
        phoneNumberHelper = mockk()

        // Default behavior: no contacts, no spam, no lists, filtering enabled
        coEvery { contactsRepository.isNumberInContacts(any()) } returns false
        coEvery { spamRepository.lookupNumber(any()) } returns null
        coEvery { userListRepository.isBlocked(any()) } returns false
        coEvery { userListRepository.isAllowed(any()) } returns false
        coEvery { settingsRepository.getFilterUnknownEnabled() } returns true
        coEvery { settingsRepository.getSpamDbEnabled() } returns true
        coEvery { settingsRepository.getBlockTelemarketersEnabled() } returns true
        coEvery { settingsRepository.getCustomTelemarketerPrefixes() } returns emptySet()

        // PhoneNumberHelper normalize: simulate realistic behavior
        // +33 -> 0, 0033 -> 0, remove spaces/dashes/dots
        every { phoneNumberHelper.normalize(any()) } answers {
            val input = firstArg<String>()
            var normalized = input.replace(Regex("[\\s\\-.()]"), "")
            if (normalized.startsWith("+33")) {
                normalized = "0" + normalized.substring(3)
            } else if (normalized.startsWith("0033")) {
                normalized = "0" + normalized.substring(4)
            }
            normalized
        }

        useCase = DecideCallActionUseCase(
            contactsRepository,
            spamRepository,
            userListRepository,
            settingsRepository,
            phoneNumberHelper
        )
    }

    private fun createSpamEntry(number: String, tag: String, score: Int) = SpamEntry(
        normalizedNumber = number,
        tag = tag,
        score = score,
        source = "test",
        lastSeen = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    // === BLOCKLIST TESTS ===

    @Test
    fun `blocked number returns Block action`() = runTest {
        coEvery { userListRepository.isBlocked("+33612345678") } returns true

        val result = useCase("+33612345678")

        assertEquals(CallAction.Block, result)
    }

    @Test
    fun `blocked number takes priority over contact`() = runTest {
        coEvery { userListRepository.isBlocked("+33612345678") } returns true
        coEvery { contactsRepository.isNumberInContacts("+33612345678") } returns true

        val result = useCase("+33612345678")

        assertEquals(CallAction.Block, result)
    }

    // === ALLOWLIST TESTS ===

    @Test
    fun `allowed number returns Allow action`() = runTest {
        coEvery { userListRepository.isAllowed("+33612345678") } returns true

        val result = useCase("+33612345678")

        assertEquals(CallAction.Allow, result)
    }

    @Test
    fun `allowed number bypasses spam check`() = runTest {
        coEvery { userListRepository.isAllowed("+33612345678") } returns true
        coEvery { spamRepository.lookupNumber("+33612345678") } returns createSpamEntry("+33612345678", "arnaque", 90)

        val result = useCase("+33612345678")

        assertEquals(CallAction.Allow, result)
    }

    // === TELEMARKETER PREFIX TESTS ===

    @Test
    fun `telemarketer prefix 0162 returns RejectAsTelemarketer`() = runTest {
        val result = useCase("0162123456")

        assertEquals(CallAction.RejectAsTelemarketer, result)
    }

    @Test
    fun `telemarketer prefix 0163 returns RejectAsTelemarketer`() = runTest {
        val result = useCase("0163999888")

        assertEquals(CallAction.RejectAsTelemarketer, result)
    }

    @Test
    fun `telemarketer prefix +33162 (normalized) returns RejectAsTelemarketer`() = runTest {
        val result = useCase("+33162123456")

        assertEquals(CallAction.RejectAsTelemarketer, result)
    }

    @Test
    fun `DOM telemarketer prefix 09475 returns RejectAsTelemarketer`() = runTest {
        val result = useCase("0947512345")

        assertEquals(CallAction.RejectAsTelemarketer, result)
    }

    @Test
    fun `telemarketer blocking disabled allows telemarketer prefix`() = runTest {
        coEvery { settingsRepository.getBlockTelemarketersEnabled() } returns false

        val result = useCase("0162123456")

        // Should be rejected as unknown (not as telemarketer)
        assertEquals(CallAction.Reject, result)
    }

    // === SPAM DATABASE TESTS ===

    @Test
    fun `spam number returns RejectAsSpam with details`() = runTest {
        coEvery { spamRepository.lookupNumber("+33612345678") } returns createSpamEntry("+33612345678", "arnaque", 85)

        val result = useCase("+33612345678")

        assertTrue(result is CallAction.RejectAsSpam)
        val spamAction = result as CallAction.RejectAsSpam
        assertEquals("arnaque", spamAction.tag)
        assertEquals(85, spamAction.score)
    }

    @Test
    fun `spam check disabled allows spam number`() = runTest {
        coEvery { settingsRepository.getSpamDbEnabled() } returns false
        coEvery { spamRepository.lookupNumber("+33612345678") } returns createSpamEntry("+33612345678", "arnaque", 90)

        val result = useCase("+33612345678")

        // Should be rejected as unknown (spam DB disabled)
        assertEquals(CallAction.Reject, result)
    }

    // === CONTACT TESTS ===

    @Test
    fun `known contact returns Allow`() = runTest {
        coEvery { contactsRepository.isNumberInContacts("+33612345678") } returns true

        val result = useCase("+33612345678")

        assertEquals(CallAction.Allow, result)
    }

    // === UNKNOWN NUMBER TESTS ===

    @Test
    fun `unknown number with filtering enabled returns Reject`() = runTest {
        val result = useCase("+33698765432")

        assertEquals(CallAction.Reject, result)
    }

    @Test
    fun `unknown number with filtering disabled returns Allow`() = runTest {
        coEvery { settingsRepository.getFilterUnknownEnabled() } returns false

        val result = useCase("+33698765432")

        assertEquals(CallAction.Allow, result)
    }

    // === PHONE NUMBER NORMALIZATION TESTS ===

    @Test
    fun `normalizes +33 prefix correctly`() = runTest {
        // Telemarketer check uses normalized number
        val result = useCase("+33162123456")

        assertEquals(CallAction.RejectAsTelemarketer, result)
    }

    @Test
    fun `normalizes 0033 prefix correctly`() = runTest {
        val result = useCase("0033162123456")

        assertEquals(CallAction.RejectAsTelemarketer, result)
    }

    @Test
    fun `removes spaces and dashes from number`() = runTest {
        val result = useCase("01 62-12.34.56")

        assertEquals(CallAction.RejectAsTelemarketer, result)
    }

    // === PRIORITY TESTS ===

    @Test
    fun `blocklist takes priority over allowlist`() = runTest {
        coEvery { userListRepository.isBlocked("+33612345678") } returns true
        coEvery { userListRepository.isAllowed("+33612345678") } returns true

        val result = useCase("+33612345678")

        assertEquals(CallAction.Block, result)
    }

    @Test
    fun `allowlist takes priority over telemarketer`() = runTest {
        coEvery { userListRepository.isAllowed("0162123456") } returns true

        val result = useCase("0162123456")

        assertEquals(CallAction.Allow, result)
    }

    @Test
    fun `telemarketer takes priority over spam`() = runTest {
        coEvery { spamRepository.lookupNumber("0162123456") } returns createSpamEntry("0162123456", "spam", 100)

        val result = useCase("0162123456")

        assertEquals(CallAction.RejectAsTelemarketer, result)
    }

    @Test
    fun `spam takes priority over contact`() = runTest {
        coEvery { contactsRepository.isNumberInContacts("+33612345678") } returns true
        coEvery { spamRepository.lookupNumber("+33612345678") } returns createSpamEntry("+33612345678", "arnaque", 80)

        val result = useCase("+33612345678")

        assertTrue(result is CallAction.RejectAsSpam)
    }

    // === CUSTOM PREFIX TESTS ===

    @Test
    fun `custom telemarketer prefix returns RejectAsTelemarketer`() = runTest {
        coEvery { settingsRepository.getCustomTelemarketerPrefixes() } returns setOf("0199")

        val result = useCase("0199123456")

        assertEquals(CallAction.RejectAsTelemarketer, result)
    }

    @Test
    fun `custom 5-digit prefix returns RejectAsTelemarketer`() = runTest {
        coEvery { settingsRepository.getCustomTelemarketerPrefixes() } returns setOf("01234")

        val result = useCase("0123456789")

        assertEquals(CallAction.RejectAsTelemarketer, result)
    }

    @Test
    fun `custom prefix combined with ARCEP works`() = runTest {
        coEvery { settingsRepository.getCustomTelemarketerPrefixes() } returns setOf("0199")

        // Test ARCEP prefix still works
        val arcepResult = useCase("0162123456")
        assertEquals(CallAction.RejectAsTelemarketer, arcepResult)

        // Test custom prefix works
        val customResult = useCase("0199123456")
        assertEquals(CallAction.RejectAsTelemarketer, customResult)
    }
}
