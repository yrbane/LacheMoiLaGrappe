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
        coEvery { settingsRepository.getSleepModeEnabled() } returns false
        coEvery { settingsRepository.getSleepModeStartTime() } returns "22:00"
        coEvery { settingsRepository.getSleepModeEndTime() } returns "07:00"
        coEvery { callLogRepository.getCallCountByNumberSince(any(), any()) } returns 0

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

    // === EMERGENCY MODE TESTS ===

    @Test
    fun `emergency mode triggers after 2 recent calls from same number`() = runTest {
        // 2 previous calls from same number in last 5 min => emergency mode (3rd attempt)
        coEvery { callLogRepository.getCallCountByNumberSince(any(), any()) } returns 2

        val result = useCase("+33698765432")

        assertEquals(CallAction.Allow, result)
    }

    @Test
    fun `emergency mode does not trigger with only 1 recent call`() = runTest {
        coEvery { callLogRepository.getCallCountByNumberSince(any(), any()) } returns 1

        val result = useCase("+33698765432")

        assertEquals(CallAction.Reject, result)
    }

    @Test
    fun `emergency mode counts per number not globally`() = runTest {
        // Only 1 call from this specific number (even if others called too)
        coEvery { callLogRepository.getCallCountByNumberSince(any(), any()) } returns 1

        val result = useCase("+33698765432")

        // Should NOT trigger emergency mode with only 1 prior call
        assertEquals(CallAction.Reject, result)
    }

    // === PUBLIC SERVICES TESTS ===

    @Test
    fun `emergency number 15 is always allowed`() = runTest {
        val result = useCase("15")
        assertEquals(CallAction.Allow, result)
    }

    @Test
    fun `SAMU 15 bypasses blocklist`() = runTest {
        coEvery { userListRepository.isBlocked("15") } returns true
        val result = useCase("15")
        assertEquals(CallAction.Allow, result)
    }

    // === SLEEP MODE TESTS ===

    @Test
    fun `sleep mode disabled does not affect filtering`() = runTest {
        coEvery { settingsRepository.getSleepModeEnabled() } returns false

        val result = useCase("+33698765432")

        assertEquals(CallAction.Reject, result)
    }
}
