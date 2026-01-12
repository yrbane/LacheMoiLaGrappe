package com.callfilter.domain.usecase

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.callfilter.domain.model.SmsStatus
import com.callfilter.domain.repository.SettingsRepository
import com.callfilter.domain.repository.SmsRepository
import com.callfilter.util.PhoneNumberHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case pour envoyer un SMS de demande d'identité.
 */
class SendIdentitySmsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val smsRepository: SmsRepository,
    private val settingsRepository: SettingsRepository,
    private val phoneNumberHelper: PhoneNumberHelper
) {
    suspend operator fun invoke(phoneNumber: String): Result<Long> {
        // Récupérer le template
        val template = settingsRepository.getSmsTemplate()
        val normalizedNumber = phoneNumberHelper.normalize(phoneNumber)
            ?: return Result.failure(IllegalArgumentException("Numéro invalide"))

        // Logger l'envoi comme pending
        val smsId = smsRepository.logSmsSent(
            number = phoneNumber,
            normalizedNumber = normalizedNumber,
            template = template,
            status = SmsStatus.PENDING
        )

        return try {
            // Envoyer le SMS
            val smsManager = context.getSystemService(SmsManager::class.java)
                ?: return Result.failure(IllegalStateException("SmsManager non disponible"))

            // Diviser le message si nécessaire
            val parts = smsManager.divideMessage(template)

            if (parts.size == 1) {
                smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    template,
                    createSentPendingIntent(smsId),
                    createDeliveryPendingIntent(smsId)
                )
            } else {
                val sentIntents = ArrayList<PendingIntent>()
                val deliveryIntents = ArrayList<PendingIntent>()
                repeat(parts.size) {
                    sentIntents.add(createSentPendingIntent(smsId))
                    deliveryIntents.add(createDeliveryPendingIntent(smsId))
                }
                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    sentIntents,
                    deliveryIntents
                )
            }

            // Mettre à jour le statut
            smsRepository.updateSmsStatus(smsId, SmsStatus.SENT)

            Result.success(smsId)
        } catch (e: Exception) {
            // Mettre à jour le statut en cas d'échec
            smsRepository.updateSmsStatus(smsId, SmsStatus.FAILED)
            Result.failure(e)
        }
    }

    private fun createSentPendingIntent(smsId: Long): PendingIntent {
        val intent = Intent(ACTION_SMS_SENT).apply {
            putExtra(EXTRA_SMS_ID, smsId)
        }
        return PendingIntent.getBroadcast(
            context,
            smsId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createDeliveryPendingIntent(smsId: Long): PendingIntent {
        val intent = Intent(ACTION_SMS_DELIVERED).apply {
            putExtra(EXTRA_SMS_ID, smsId)
        }
        return PendingIntent.getBroadcast(
            context,
            smsId.toInt() + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_SMS_SENT = "com.callfilter.SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.callfilter.SMS_DELIVERED"
        const val EXTRA_SMS_ID = "sms_id"
    }
}
