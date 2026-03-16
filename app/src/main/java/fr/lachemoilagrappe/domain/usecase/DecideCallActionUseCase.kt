package fr.lachemoilagrappe.domain.usecase

import fr.lachemoilagrappe.domain.model.CallAction
import fr.lachemoilagrappe.domain.repository.CallLogRepository
import fr.lachemoilagrappe.domain.repository.ContactsRepository
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import fr.lachemoilagrappe.domain.repository.UserListRepository
import fr.lachemoilagrappe.util.PhoneNumberHelper
import java.util.Calendar
import javax.inject.Inject

/**
 * Use case pour décider de l'action à effectuer sur un appel entrant.
 *
 * Priorité de décision :
 * 0. Services Publics & Urgences (Whitelist embarquée) → Allow
 * 0.5 Mode Urgence (3 appels en < 5 min) → Allow
 * 1. Blocklist utilisateur → Block
 * 2. Allowlist utilisateur → Allow
 * 3. Préfixes démarcheurs (si activé) → RejectAsTelemarketer
 * 4. Contact connu → Allow
 * 5. Inconnu (si filtrage activé) → Reject
 * 6. Sinon → Allow
 */
class DecideCallActionUseCase @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val userListRepository: UserListRepository,
    private val settingsRepository: SettingsRepository,
    private val callLogRepository: CallLogRepository,
    private val phoneNumberHelper: PhoneNumberHelper
) {
    companion object {
        // Préfixes réservés aux démarcheurs (ARCEP)
        private val ARCEP_PREFIXES_LIST = listOf(
            "0162", "0163", "0270", "0271", "0377", "0378",
            "0424", "0425", "0568", "0569", "0948", "0949",
            "09475", "09476", "09477", "09478", "09479"
        )

        val ARCEP_PREFIXES: Set<String> = ARCEP_PREFIXES_LIST.toSet()

        // Services publics et numéros d'urgence (toujours autorisés)
        private val PUBLIC_SERVICES = setOf(
            "15", "17", "18", "112", "114", "115", "119", "191", "196", "197",
            "3646", // Ameli
            "3117", // SNCF Urgence
            "3919", // Violences Femmes Info
            "3020"  // Non au harcèlement
        )
        
        private const val EMERGENCY_WINDOW_MS = 5 * 60 * 1000L // 5 minutes
        private const val EMERGENCY_ATTEMPTS = 3
    }

    suspend operator fun invoke(phoneNumber: String): CallAction {
        // Normaliser le numéro
        val normalizedNumber = phoneNumberHelper.normalize(phoneNumber) ?: normalizeBasic(phoneNumber)

        // 0. Vérifier les services publics (priorité absolue pour être joignable)
        if (PUBLIC_SERVICES.contains(normalizedNumber) || PUBLIC_SERVICES.contains(phoneNumber)) {
            return CallAction.Allow
        }

        // 0.1 Vérifier le mode sommeil : si activé et dans la plage horaire, on laisse tout passer
        // (La sérénité c'est aussi de ne pas rater d'appels la nuit si on ne veut pas de filtrage)
        if (settingsRepository.getSleepModeEnabled() && isCurrentlyInSleepMode()) {
            return CallAction.Allow
        }

        // 0.5 Mode Urgence : si le MÊME numéro appelle 3 fois en moins de 5 minutes
        val recentAttempts = callLogRepository.getCallCountByNumberSince(
            normalizedNumber,
            System.currentTimeMillis() - EMERGENCY_WINDOW_MS
        )
        if (recentAttempts >= EMERGENCY_ATTEMPTS - 1) { // -1 car l'appel actuel n'est pas encore loggé
            return CallAction.Allow
        }

        // 1. Vérifier la blocklist utilisateur
        if (userListRepository.isBlocked(phoneNumber)) {
            return CallAction.Block
        }

        // 2. Vérifier l'allowlist utilisateur
        if (userListRepository.isAllowed(phoneNumber)) {
            return CallAction.Allow
        }

        // 3. Vérifier les préfixes démarcheurs
        if (settingsRepository.getBlockTelemarketersEnabled()) {
            val customPrefixes = settingsRepository.getCustomTelemarketerPrefixes()
            if (isTelemarketerNumber(normalizedNumber, customPrefixes)) {
                return CallAction.RejectAsTelemarketer
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

    private fun normalizeBasic(phoneNumber: String): String {
        var normalized = phoneNumber.replace(Regex("[\\s\\-.]"), "")
        if (normalized.startsWith("+33")) normalized = "0" + normalized.substring(3)
        if (normalized.startsWith("0033")) normalized = "0" + normalized.substring(4)
        return normalized
    }

    private fun isTelemarketerNumber(normalizedNumber: String, customPrefixes: Set<String>): Boolean {
        val allPrefixes = (ARCEP_PREFIXES + customPrefixes).sortedByDescending { it.length }
        return allPrefixes.any { prefix -> normalizedNumber.startsWith(prefix) }
    }

    private suspend fun isCurrentlyInSleepMode(): Boolean {
        val startStr = settingsRepository.getSleepModeStartTime()
        val endStr = settingsRepository.getSleepModeEndTime()
        
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        
        val startMinutes = parseMinutes(startStr) ?: return false
        val endMinutes = parseMinutes(endStr) ?: return false
        
        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes..endMinutes
        } else {
            // Case where interval crosses midnight (ex: 22:00 to 07:00)
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }

    private fun parseMinutes(timeStr: String): Int? {
        val parts = timeStr.split(":")
        if (parts.size != 2) return null
        return try {
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            hours * 60 + minutes
        } catch (e: Exception) {
            null
        }
    }
}
