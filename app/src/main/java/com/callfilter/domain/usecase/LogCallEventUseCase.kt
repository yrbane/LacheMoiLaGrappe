package com.callfilter.domain.usecase

import com.callfilter.data.local.db.entity.CallLogEntry
import com.callfilter.domain.model.CallAction
import com.callfilter.domain.model.CallDecision
import com.callfilter.domain.repository.CallLogRepository
import com.callfilter.domain.repository.ContactsRepository
import com.callfilter.util.PhoneNumberHelper
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
            is CallAction.RejectAsSpam -> CallDecision.REJECTED_SPAM
            is CallAction.Block -> CallDecision.BLOCKED
        }

        val reason = when (action) {
            is CallAction.Allow -> if (contactName != null) "contact" else "allowlist"
            is CallAction.Reject -> "unknown"
            is CallAction.RejectAsSpam -> "spam: ${action.tag}"
            is CallAction.Block -> "blocklist"
        }

        val entry = CallLogEntry(
            phoneNumber = phoneNumber,
            normalizedNumber = normalizedNumber,
            timestamp = System.currentTimeMillis(),
            decision = decision,
            reason = reason,
            contactName = contactName,
            spamTag = (action as? CallAction.RejectAsSpam)?.tag,
            spamScore = (action as? CallAction.RejectAsSpam)?.score
        )

        return callLogRepository.logCall(entry)
    }
}
