package com.callfilter.domain.usecase

import com.callfilter.domain.model.SmsDecision
import com.callfilter.domain.repository.SettingsRepository
import com.callfilter.domain.repository.SmsRepository
import com.callfilter.util.PhoneNumberHelper
import javax.inject.Inject

/**
 * Use case pour décider si un SMS doit être envoyé à un appelant inconnu.
 */
class ShouldSendSmsUseCase @Inject constructor(
    private val smsRepository: SmsRepository,
    private val settingsRepository: SettingsRepository,
    private val phoneNumberHelper: PhoneNumberHelper
) {
    suspend operator fun invoke(phoneNumber: String): SmsDecision {
        // Vérifier si la fonctionnalité SMS est activée
        if (!settingsRepository.getAutoSmsEnabled()) {
            return SmsDecision.Skip("SMS automatique désactivé")
        }

        // Vérifier si le numéro doit être exclu
        if (phoneNumberHelper.shouldExcludeFromSms(phoneNumber)) {
            return SmsDecision.Skip("Numéro exclu (urgence, court ou masqué)")
        }

        // Vérifier si c'est un numéro mobile (optionnel mais recommandé)
        if (!phoneNumberHelper.isMobileNumber(phoneNumber)) {
            return SmsDecision.Skip("Numéro non mobile")
        }

        // Vérifier le cooldown
        val cooldownHours = settingsRepository.getSmsCooldownHours()
        if (!smsRepository.canSendSms(phoneNumber, cooldownHours)) {
            return SmsDecision.Skip("Cooldown non écoulé")
        }

        // Vérifier si le mode confirmation est activé
        if (settingsRepository.getSmsConfirmationMode()) {
            return SmsDecision.AskConfirmation
        }

        return SmsDecision.Send
    }
}
