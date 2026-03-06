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

class DecideCallActionIntegrationTest {

    private val contactsRepository: ContactsRepository = mockk()
    private val userListRepository: UserListRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val callLogRepository: CallLogRepository = mockk()
    private val phoneNumberHelper: PhoneNumberHelper = mockk()
    
    private lateinit var useCase: DecideCallActionUseCase

    @Before
    fun setup() {
        useCase = DecideCallActionUseCase(
            contactsRepository,
            userListRepository,
            settingsRepository,
            callLogRepository,
            phoneNumberHelper
        )
        // Default behavior: nothing in lists, unknown number, filtering ON, no recent calls
        every { phoneNumberHelper.normalize(any()) } answers { it.invocation.args[0] as String }
        coEvery { userListRepository.isBlocked(any()) } returns false
        coEvery { userListRepository.isAllowed(any()) } returns false
        coEvery { settingsRepository.getBlockTelemarketersEnabled() } returns true
        coEvery { settingsRepository.getCustomTelemarketerPrefixes() } returns emptySet()
        coEvery { contactsRepository.isNumberInContacts(any()) } returns false
        coEvery { settingsRepository.getFilterUnknownEnabled() } returns true
        coEvery { callLogRepository.getCallCountSince(any()) } returns 0
    }

    @Test
    fun `priority 0 - emergency mode allows call if 3 attempts in 5 minutes`() = runTest {
        val number = "0700000000"
        coEvery { callLogRepository.getCallCountSince(any()) } returns 3
        
        val result = useCase(number)
        assertEquals(CallAction.Allow, result)
    }

    @Test
    fun `public service number is always allowed even if not in contacts`() = runTest {
        val samu = "15"
        val police = "17"
        val ameli = "3646"
        
        assertEquals(CallAction.Allow, useCase(samu))
        assertEquals(CallAction.Allow, useCase(police))
        assertEquals(CallAction.Allow, useCase(ameli))
    }

    @Test
    fun `priority 1 - blocklist takes absolute precedence`() = runTest {
        val number = "0612345678"
        coEvery { userListRepository.isBlocked(number) } returns true
        coEvery { userListRepository.isAllowed(number) } returns true // Should be ignored
        
        val result = useCase(number)
        assertEquals(CallAction.Block, result)
    }

    @Test
    fun `priority 2 - allowlist takes precedence over telemarketers`() = runTest {
        val telemarketer = "0162123456"
        coEvery { userListRepository.isAllowed(telemarketer) } returns true
        
        val result = useCase(telemarketer)
        assertEquals(CallAction.Allow, result)
    }

    @Test
    fun `priority 3 - telemarketer prefix is rejected`() = runTest {
        val telemarketer = "0162123456"
        val result = useCase(telemarketer)
        assertEquals(CallAction.RejectAsTelemarketer, result)
    }

    @Test
    fun `priority 4 - contact is allowed`() = runTest {
        val number = "0611223344"
        coEvery { contactsRepository.isNumberInContacts(number) } returns true
        
        val result = useCase(number)
        assertEquals(CallAction.Allow, result)
    }

    @Test
    fun `priority 5 - unknown number is rejected if filtering enabled`() = runTest {
        val number = "0700000000"
        val result = useCase(number)
        assertEquals(CallAction.Reject, result)
    }
}
