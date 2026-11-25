package fr.lachemoilagrappe.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneNumberHelper @Inject constructor() {

    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    companion object {
        const val DEFAULT_REGION = "FR"
        private val EMERGENCY_NUMBERS = setOf("15", "17", "18", "112", "114", "115", "119")
        private val SHORT_NUMBER_PREFIXES = setOf("3", "08")
        private val NON_PHONE_CHARS_REGEX = Regex("[^+\\d]")
        private val NON_DIGITS_REGEX = Regex("[^\\d]")
    }

    /**
     * Normalise un numéro de téléphone au format E.164
     * @return Le numéro normalisé ou null si invalide
     */
    fun normalize(phoneNumber: String?): String? {
        if (phoneNumber.isNullOrBlank()) return null

        return try {
            val parsed = phoneUtil.parse(phoneNumber, DEFAULT_REGION)
            if (phoneUtil.isValidNumber(parsed)) {
                phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
            } else {
                // Garder le numéro tel quel s'il ne peut pas être validé
                phoneNumber.replace(NON_PHONE_CHARS_REGEX, "")
            }
        } catch (e: NumberParseException) {
            // Garder le numéro tel quel en cas d'erreur
            phoneNumber.replace(NON_PHONE_CHARS_REGEX, "")
        }
    }

    /**
     * Vérifie si un numéro est probablement un mobile
     * @return true si le numéro semble être un mobile
     */
    fun isMobileNumber(phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return false

        if (isFrenchMobileHeuristic(phoneNumber)) return true
        if (isFrenchFixedLineHeuristic(phoneNumber)) return false

        return try {
            val parsed = phoneUtil.parse(phoneNumber, DEFAULT_REGION)
            val numberType = phoneUtil.getNumberType(parsed)

            numberType == PhoneNumberUtil.PhoneNumberType.MOBILE ||
                numberType == PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE
        } catch (e: NumberParseException) {
            false
        }
    }

    /**
     * Vérifie si un numéro est probablement une ligne fixe française (01 à 05)
     * @return true si le numéro semble être une ligne fixe
     */
    fun isFixedLine(phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return false

        if (isFrenchFixedLineHeuristic(phoneNumber)) return true
        if (isFrenchMobileHeuristic(phoneNumber)) return false

        return try {
            val parsed = phoneUtil.parse(phoneNumber, DEFAULT_REGION)
            val numberType = phoneUtil.getNumberType(parsed)

            numberType == PhoneNumberUtil.PhoneNumberType.FIXED_LINE
        } catch (e: NumberParseException) {
            false
        }
    }

    /**
     * Heuristique simple pour détecter une ligne fixe géographique française (01 à 05)
     */
    private fun isFrenchFixedLineHeuristic(phoneNumber: String): Boolean {
        val cleaned = phoneNumber.replace(NON_DIGITS_REGEX, "")
        val national = when {
            cleaned.startsWith("0033") && cleaned.length == 13 -> "0" + cleaned.substring(4)
            cleaned.startsWith("33") && cleaned.length == 11 -> "0" + cleaned.substring(2)
            else -> cleaned
        }
        return national.length == 10 && (
            national.startsWith("01") ||
                national.startsWith("02") ||
                national.startsWith("03") ||
                national.startsWith("04") ||
                national.startsWith("05")
            )
    }

    /**
     * Heuristique simple pour détecter un mobile français
     */
    private fun isFrenchMobileHeuristic(phoneNumber: String): Boolean {
        val cleaned = phoneNumber.replace(NON_DIGITS_REGEX, "")

        // Numéros français mobiles : 06, 07 ou +336, +337, 00336, 00337
        return when {
            cleaned.startsWith("00336") || cleaned.startsWith("00337") -> true
            cleaned.startsWith("336") || cleaned.startsWith("337") -> true
            cleaned.startsWith("06") || cleaned.startsWith("07") -> true
            else -> false
        }
    }

    /**
     * Vérifie si le numéro est un numéro d'urgence
     */
    fun isEmergencyNumber(phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return false
        val cleaned = phoneNumber.replace(NON_DIGITS_REGEX, "")
        return cleaned in EMERGENCY_NUMBERS
    }

    /**
     * Vérifie si le numéro est un numéro court (services, etc.)
     */
    fun isShortNumber(phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return false
        val cleaned = phoneNumber.replace(NON_DIGITS_REGEX, "")

        // Numéros courts : moins de 6 chiffres ou commençant par 3xxx, 08xx
        return cleaned.length < 6 ||
            SHORT_NUMBER_PREFIXES.any { cleaned.startsWith(it) }
    }

    /**
     * Vérifie si un numéro est masqué ou invalide
     */
    fun isHiddenOrInvalid(phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return true
        val cleaned = phoneNumber.replace(NON_PHONE_CHARS_REGEX, "")
        return cleaned.isEmpty() || cleaned == "0" || cleaned.startsWith("-")
    }

    /**
     * Formate un numéro pour l'affichage
     */
    fun formatForDisplay(phoneNumber: String?): String {
        if (phoneNumber.isNullOrBlank()) return "Numéro masqué"

        return try {
            val parsed = phoneUtil.parse(phoneNumber, DEFAULT_REGION)
            phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
        } catch (e: NumberParseException) {
            phoneNumber
        }
    }

    /**
     * Vérifie si le numéro doit être exclu des SMS automatiques
     */
    fun shouldExcludeFromSms(phoneNumber: String?): Boolean {
        return isHiddenOrInvalid(phoneNumber) ||
            isEmergencyNumber(phoneNumber) ||
            isShortNumber(phoneNumber)
    }
}
