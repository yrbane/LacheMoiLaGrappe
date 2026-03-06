package fr.lachemoilagrappe.domain.usecase

import fr.lachemoilagrappe.domain.model.CallAction
import fr.lachemoilagrappe.domain.repository.CallLogRepository
import fr.lachemoilagrappe.domain.repository.ContactsRepository
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import fr.lachemoilagrappe.domain.repository.UserListRepository
import fr.lachemoilagrappe.util.PhoneNumberHelper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DecideCallActionUseCaseTest {

    private lateinit var contactsRepository: ContactsRepository
    private lateinit var userListRepository: UserListRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var callLogRepository: CallLogRepository
    private lateinit var phoneNumberHelper: PhoneNumberHelper
    private lateinit var useCase: DecideCallActionUseCase

    @Before
    fun setup() {
        contactsRepository = mockk()
        userListRepository = mockk()
        settingsRepository = mockk()
        callLogRepository = mockk()
        phoneNumberHelper = mockk()

        // Default behavior: no contacts, no lists, filtering enabled, no recent calls
        coEvery { contactsRepository.isNumberInContacts(any()) } returns false
        coEvery { userListRepository.isBlocked(any()) } returns false
        coEvery { userListRepository.isAllowed(any()) } returns false
        coEvery { settingsRepository.getFilterUnknownEnabled() } returns true
        coEvery { settingsRepository.getBlockTelemarketersEnabled() } returns true
        coEvery { settingsRepository.getCustomTelemarketerPrefixes() } returns emptySet()
        coEvery { callLogRepository.getCallCountSince(any()) } returns 0

        // PhoneNumberHelper normalize: simulate realistic behavior
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
            userListRepository,
            settingsRepository,
            callLogRepository,
            phoneNumberHelper
        )
    }

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

    // === TELEMARKETER PREFIX TESTS ===

    @Test
    fun `telemarketer prefix 0162 returns RejectAsTelemarketer`() = runTest {
        val result = useCase("0162123456")

        assertEquals(CallAction.RejectAsTelemarketer, result)
    }

    @Test
    fun `telemarketer blocking disabled allows telemarketer prefix`() = runTest {
        coEvery { settingsRepository.getBlockTelemarketersEnabled() } returns false

        val result = useCase("0162123456")

        // Should be rejected as unknown (not as telemarketer)
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
}
