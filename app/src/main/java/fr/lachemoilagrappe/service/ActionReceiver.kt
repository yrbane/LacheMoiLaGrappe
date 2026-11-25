package fr.lachemoilagrappe.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import fr.lachemoilagrappe.domain.repository.UserListRepository
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import fr.lachemoilagrappe.domain.usecase.SendIdentitySmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class ActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userListRepository: UserListRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var sendIdentitySmsUseCase: SendIdentitySmsUseCase

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val phoneNumber = intent.getStringExtra(NotificationHelper.EXTRA_PHONE_NUMBER) ?: return

        // Annuler la notification avec l'ID sécurisé utilisé lors de sa création
        notificationHelper.cancelNotificationsForNumber(phoneNumber)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    NotificationHelper.ACTION_ALLOW -> {
                        userListRepository.addToAllowlist(phoneNumber)
                    }
                    NotificationHelper.ACTION_BLOCK -> {
                        userListRepository.addToBlocklist(phoneNumber)
                    }
                    NotificationHelper.ACTION_SEND_SMS -> {
                        val result = sendIdentitySmsUseCase(phoneNumber)
                        notificationHelper.showSmsSentNotification(
                            phoneNumber,
                            result.isSuccess
                        )
                    }
                    NotificationHelper.ACTION_PREPARE_SMS -> {
                        val template = settingsRepository.smsTemplate.first()
                        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "smsto:$phoneNumber".toUri()
                            putExtra("sms_body", template)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(smsIntent)
                    }
                    NotificationHelper.ACTION_DISMISS -> {
                        // Juste fermer la notification
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
