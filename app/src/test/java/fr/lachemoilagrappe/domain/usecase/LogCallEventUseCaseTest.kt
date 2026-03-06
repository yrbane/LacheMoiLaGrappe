package fr.lachemoilagrappe.domain.usecase

import fr.lachemoilagrappe.domain.model.CallAction
import fr.lachemoilagrappe.domain.model.CallDecision
import fr.lachemoilagrappe.domain.repository.CallLogRepository
import fr.lachemoilagrappe.domain.repository.ContactsRepository
import fr.lachemoilagrappe.util.PhoneNumberHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LogCallEventUseCaseTest {

    private val callLogRepository: CallLogRepository = mockk()
    private val contactsRepository: ContactsRepository = mockk()
    private val phoneNumberHelper: PhoneNumberHelper = mockk()
    private val useCase = LogCallEventUseCase(callLogRepository, contactsRepository, phoneNumberHelper)

    @Test
    fun `when logging call, correct entry is sent to repository`() = runTest {
        val phoneNumber = "0612345678"
        val normalized = "0612345678"
        val action = CallAction.RejectAsTelemarketer
        
        every { phoneNumberHelper.normalize(phoneNumber) } returns normalized
        coEvery { contactsRepository.getContactName(phoneNumber) } returns "Spam Test"
        coEvery { callLogRepository.logCall(any()) } returns 1L

        useCase(phoneNumber, action)

        coVerify {
            callLogRepository.logCall(match {
                it.phoneNumber == phoneNumber &&
                it.normalizedNumber == normalized &&
                it.decision == CallDecision.REJECTED_TELEMARKETER &&
                it.contactName == "Spam Test"
            })
        }
    }
}
