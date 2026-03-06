package fr.lachemoilagrappe.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import fr.lachemoilagrappe.domain.repository.SmsRepository
import fr.lachemoilagrappe.domain.usecase.AnalyzeSmsContentUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var analyzeSmsContentUseCase: AnalyzeSmsContentUseCase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var smsRepository: SmsRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Combine multipart SMS into a single string
        val fullMessage = StringBuilder()
        var sender: String? = null

        for (sms in messages) {
            if (sms != null) {
                fullMessage.append(sms.displayMessageBody)
                if (sender == null) {
                    sender = sms.displayOriginatingAddress
                }
            }
        }

        if (sender == null || fullMessage.isEmpty()) return

        val messageBody = fullMessage.toString()

        scope.launch {
            try {
                val isProtectionEnabled = settingsRepository.getPhishingProtectionEnabled()
                if (!isProtectionEnabled) return@launch

                val analysisResult = analyzeSmsContentUseCase(messageBody)

                if (analysisResult.isPhishing) {
                    Timber.d("Phishing SMS detected from $sender: ${analysisResult.matchedKeyword}")
                    
                    // Log in database
                    smsRepository.logPhishingSms(
                        phoneNumber = sender,
                        body = messageBody,
                        matchedKeyword = analysisResult.matchedKeyword
                    )

                    notificationHelper.showPhishingSmsNotification(
                        phoneNumber = sender,
                        keyword = analysisResult.matchedKeyword
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error analyzing incoming SMS")
            }
        }
    }
}
