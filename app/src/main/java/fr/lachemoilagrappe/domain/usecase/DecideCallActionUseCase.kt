package fr.lachemoilagrappe.domain.usecase

import fr.lachemoilagrappe.domain.model.CallAction
import fr.lachemoilagrappe.domain.repository.ContactsRepository
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import fr.lachemoilagrappe.domain.repository.SpamRepository
import fr.lachemoilagrappe.domain.repository.UserListRepository
import fr.lachemoilagrappe.util.PhoneNumberHelper
import javax.inject.Inject

/**
 * Use case pour décider de l'action à effectuer sur un appel entrant.
 *
 * Priorité de décision :
 * 1. Blocklist utilisateur → Block
 * 2. Allowlist utilisateur → Allow
 * 3. Préfixes démarcheurs (si activé) → RejectAsTelemarketer
 * 4. Base spam (si activée) → RejectAsSpam
 * 5. Contact connu → Allow
 * 6. Inconnu (si filtrage activé) → Reject
 * 7. Sinon → Allow
 */
class DecideCallActionUseCase @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val spamRepository: SpamRepository,
    private val userListRepository: UserListRepository,
    private val settingsRepository: SettingsRepository,
    private val phoneNumberHelper: PhoneNumberHelper
) {
    companion object {
        // Préfixes réservés aux démarcheurs (ARCEP)
        // France métropolitaine
        private val TELEMARKETER_PREFIXES_METRO = listOf(
            "0162", "0163", "0270", "0271", "0377", "0378",
            "0424", "0425", "0568", "0569", "0948", "0949"
        )
        // DOM-TOM
        private val TELEMARKETER_PREFIXES_DOM = listOf(
            "09475", // Guadeloupe, Saint-Martin, Saint-Barthélemy
            "09476", // Guyane
            "09477", // Martinique
            "09478", "09479" // La Réunion, Mayotte
        )

        // Official ARCEP prefixes (exposed for UI display)
        val ARCEP_PREFIXES: Set<String> = (TELEMARKETER_PREFIXES_METRO + TELEMARKETER_PREFIXES_DOM).toSet()

        // Use sorted list for efficient prefix matching
        // Longer prefixes first to match most specific first
        private val SORTED_ARCEP_PREFIXES = ARCEP_PREFIXES.sortedByDescending { it.length }
    }

    suspend operator fun invoke(phoneNumber: String): CallAction {
        // Normaliser le numéro avec PhoneNumberHelper
        val normalizedNumber = phoneNumberHelper.normalize(phoneNumber) ?: normalizeBasic(phoneNumber)

        // 1. Vérifier la blocklist utilisateur (priorité absolue)
        if (userListRepository.isBlocked(phoneNumber)) {
            return CallAction.Block
        }

        // 2. Vérifier l'allowlist utilisateur
        if (userListRepository.isAllowed(phoneNumber)) {
            return CallAction.Allow
        }

        // 3. Vérifier les préfixes démarcheurs si activé
        if (settingsRepository.getBlockTelemarketersEnabled()) {
            val customPrefixes = settingsRepository.getCustomTelemarketerPrefixes()
            if (isTelemarketerNumber(normalizedNumber, customPrefixes)) {
                return CallAction.RejectAsTelemarketer
            }
        }

        // 4. Vérifier la base spam si activée
        if (settingsRepository.getSpamDbEnabled()) {
            val spamEntry = spamRepository.lookupNumber(phoneNumber)
            if (spamEntry != null) {
                return CallAction.RejectAsSpam(
                    tag = spamEntry.tag,
                    score = spamEntry.score
                )
            }
        }

        // 5. Vérifier si c'est un contact connu
        if (contactsRepository.isNumberInContacts(phoneNumber)) {
            return CallAction.Allow
        }

        // 6. Si filtrage des inconnus activé, rejeter
        if (settingsRepository.getFilterUnknownEnabled()) {
            return CallAction.Reject
        }

        // 7. Sinon, laisser passer
        return CallAction.Allow
    }

    /**
     * Basic normalization fallback if PhoneNumberHelper fails.
     * Removes spaces, dashes, dots and converts +33/0033 to 0.
     */
    private fun normalizeBasic(phoneNumber: String): String {
        var normalized = phoneNumber.replace(Regex("[\\s\\-.]"), "")
        // Convertir +33 en 0
        if (normalized.startsWith("+33")) {
            normalized = "0" + normalized.substring(3)
        }
        // Convertir 0033 en 0
        if (normalized.startsWith("0033")) {
            normalized = "0" + normalized.substring(4)
        }
        return normalized
    }

    private fun isTelemarketerNumber(normalizedNumber: String, customPrefixes: Set<String>): Boolean {
        // Combine ARCEP + custom prefixes, sorted by length (longer first) for most specific match
        val allPrefixes = (ARCEP_PREFIXES + customPrefixes).sortedByDescending { it.length }
        // O(n) but n is typically small (17 ARCEP + few custom)
        return allPrefixes.any { prefix ->
            normalizedNumber.startsWith(prefix)
        }
    }
}
