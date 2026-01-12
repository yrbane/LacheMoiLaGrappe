package com.callfilter.domain.usecase

import com.callfilter.domain.model.CallAction
import com.callfilter.domain.repository.ContactsRepository
import com.callfilter.domain.repository.SettingsRepository
import com.callfilter.domain.repository.SpamRepository
import com.callfilter.domain.repository.UserListRepository
import javax.inject.Inject

/**
 * Use case pour décider de l'action à effectuer sur un appel entrant.
 *
 * Priorité de décision :
 * 1. Blocklist utilisateur → Block
 * 2. Allowlist utilisateur → Allow
 * 3. Base spam (si activée) → RejectAsSpam
 * 4. Contact connu → Allow
 * 5. Inconnu (si filtrage activé) → Reject
 * 6. Sinon → Allow
 */
class DecideCallActionUseCase @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val spamRepository: SpamRepository,
    private val userListRepository: UserListRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(phoneNumber: String): CallAction {
        // 1. Vérifier la blocklist utilisateur (priorité absolue)
        if (userListRepository.isBlocked(phoneNumber)) {
            return CallAction.Block
        }

        // 2. Vérifier l'allowlist utilisateur
        if (userListRepository.isAllowed(phoneNumber)) {
            return CallAction.Allow
        }

        // 3. Vérifier la base spam si activée
        if (settingsRepository.getSpamDbEnabled()) {
            val spamEntry = spamRepository.lookupNumber(phoneNumber)
            if (spamEntry != null) {
                return CallAction.RejectAsSpam(
                    tag = spamEntry.tag,
                    score = spamEntry.score
                )
            }
        }

        // 4. Vérifier si c'est un contact connu
        if (contactsRepository.isNumberInContacts(phoneNumber)) {
            return CallAction.Allow
        }

        // 5. Si filtrage des inconnus activé, rejeter
        if (settingsRepository.getFilterUnknownEnabled()) {
            return CallAction.Reject
        }

        // 6. Sinon, laisser passer
        return CallAction.Allow
    }
}
