package fr.lachemoilagrappe.domain.usecase

import fr.lachemoilagrappe.data.local.db.entity.CallLogEntry
import fr.lachemoilagrappe.domain.model.CallAction
import fr.lachemoilagrappe.domain.model.CallDecision
import fr.lachemoilagrappe.domain.repository.CallLogRepository
import fr.lachemoilagrappe.domain.repository.ContactsRepository
import fr.lachemoilagrappe.util.PhoneNumberHelper
import javax.inject.Inject

/**
 * Use case pour journaliser un événement d'appel filtré.
 */
class LogCallEventUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val contactsRepository: ContactsRepository,
    private val phoneNumberHelper: PhoneNumberHelper
) {
    suspend operator fun invoke(
        phoneNumber: String,
        action: CallAction
    ): Long {
        val normalizedNumber = phoneNumberHelper.normalize(phoneNumber) ?: phoneNumber
        val contactName = contactsRepository.getContactName(phoneNumber)

        val decision = when (action) {
            is CallAction.Allow -> CallDecision.ALLOWED
            is CallAction.Reject -> CallDecision.REJECTED
            is CallAction.RejectAsTelemarketer -> CallDecision.REJECTED_TELEMARKETER
            is CallAction.RejectAsHidden -> CallDecision.REJECTED_HIDDEN
            is CallAction.Block -> CallDecision.BLOCKED
        }

        val reason = when (action) {
            is CallAction.Allow -> if (contactName != null) "contact" else "allowlist"
            is CallAction.Reject -> "unknown"
            is CallAction.RejectAsTelemarketer -> "telemarketer"
            is CallAction.RejectAsHidden -> "hidden"
            is CallAction.Block -> "blocklist"
        }

        val entry = CallLogEntry(
            phoneNumber = phoneNumber,
            normalizedNumber = normalizedNumber,
            timestamp = System.currentTimeMillis(),
            decision = decision,
            reason = reason,
            contactName = contactName
        )

        return callLogRepository.logCall(entry)
    }
}
