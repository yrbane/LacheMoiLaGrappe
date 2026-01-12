package com.callfilter.util

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
                phoneNumber.replace(Regex("[^+\\d]"), "")
            }
        } catch (e: NumberParseException) {
            // Garder le numéro tel quel en cas d'erreur
            phoneNumber.replace(Regex("[^+\\d]"), "")
        }
    }

    /**
     * Vérifie si un numéro est probablement un mobile
     * @return true si le numéro semble être un mobile
     */
    fun isMobileNumber(phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return false

        return try {
            val parsed = phoneUtil.parse(phoneNumber, DEFAULT_REGION)
            val numberType = phoneUtil.getNumberType(parsed)

            numberType == PhoneNumberUtil.PhoneNumberType.MOBILE ||
                numberType == PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE
        } catch (e: NumberParseException) {
            // Heuristique pour les numéros français
            isFrenchMobileHeuristic(phoneNumber)
        }
    }

    /**
     * Heuristique simple pour détecter un mobile français
     */
    private fun isFrenchMobileHeuristic(phoneNumber: String): Boolean {
        val cleaned = phoneNumber.replace(Regex("[^\\d]"), "")

        // Numéros français mobiles : 06, 07 ou +336, +337
        return when {
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
        val cleaned = phoneNumber.replace(Regex("[^\\d]"), "")
        return cleaned in EMERGENCY_NUMBERS
    }

    /**
     * Vérifie si le numéro est un numéro court (services, etc.)
     */
    fun isShortNumber(phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return false
        val cleaned = phoneNumber.replace(Regex("[^\\d]"), "")

        // Numéros courts : moins de 6 chiffres ou commençant par 3xxx, 08xx
        return cleaned.length < 6 ||
            SHORT_NUMBER_PREFIXES.any { cleaned.startsWith(it) }
    }

    /**
     * Vérifie si un numéro est masqué ou invalide
     */
    fun isHiddenOrInvalid(phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return true
        val cleaned = phoneNumber.replace(Regex("[^+\\d]"), "")
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
