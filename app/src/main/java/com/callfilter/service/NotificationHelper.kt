package com.callfilter.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.callfilter.R
import com.callfilter.domain.model.CallAction
import com.callfilter.ui.MainActivity
import com.callfilter.util.PhoneNumberHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val phoneNumberHelper: PhoneNumberHelper
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_REJECTED,
                    context.getString(R.string.notification_channel_rejected),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications pour les appels rejetés"
                },
                NotificationChannel(
                    CHANNEL_SPAM,
                    context.getString(R.string.notification_channel_spam),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications pour les spams détectés"
                },
                NotificationChannel(
                    CHANNEL_SMS,
                    context.getString(R.string.notification_channel_sms),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Notifications pour les SMS envoyés"
                }
            )

            val manager = context.getSystemService(NotificationManager::class.java)
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    fun showRejectedCallNotification(phoneNumber: String, action: CallAction) {
        val displayNumber = phoneNumberHelper.formatForDisplay(phoneNumber)

        val (channelId, title, text) = when (action) {
            is CallAction.RejectAsSpam -> Triple(
                CHANNEL_SPAM,
                context.getString(R.string.notification_spam_title),
                "Spam détecté : ${action.tag} (score ${action.score}) - $displayNumber"
            )
            is CallAction.Block -> Triple(
                CHANNEL_REJECTED,
                context.getString(R.string.notification_rejected_title),
                "Numéro bloqué : $displayNumber"
            )
            else -> Triple(
                CHANNEL_REJECTED,
                context.getString(R.string.notification_rejected_title),
                "Appel inconnu rejeté : $displayNumber"
            )
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            phoneNumber.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_add,
                context.getString(R.string.allow),
                createAllowPendingIntent(phoneNumber)
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                context.getString(R.string.block),
                createBlockPendingIntent(phoneNumber)
            )
            .build()

        try {
            notificationManager.notify(phoneNumber.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission non accordée
        }
    }

    fun showSmsConfirmationNotification(phoneNumber: String) {
        val displayNumber = phoneNumberHelper.formatForDisplay(phoneNumber)

        val notification = NotificationCompat.Builder(context, CHANNEL_SMS)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("Envoyer un SMS ?")
            .setContentText("Voulez-vous envoyer un SMS à $displayNumber ?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_send,
                "Envoyer",
                createSendSmsPendingIntent(phoneNumber)
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Ignorer",
                createDismissPendingIntent(phoneNumber)
            )
            .build()

        try {
            notificationManager.notify(SMS_CONFIRMATION_ID_BASE + phoneNumber.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission non accordée
        }
    }

    fun showSmsSentNotification(phoneNumber: String, success: Boolean) {
        val displayNumber = phoneNumberHelper.formatForDisplay(phoneNumber)
        val text = if (success) {
            "SMS envoyé à $displayNumber"
        } else {
            "Échec d'envoi du SMS à $displayNumber"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_SMS)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("SMS")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(SMS_SENT_ID_BASE + phoneNumber.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission non accordée
        }
    }

    private fun createAllowPendingIntent(phoneNumber: String): PendingIntent {
        val intent = Intent(context, ActionReceiver::class.java).apply {
            action = ACTION_ALLOW
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
        }
        return PendingIntent.getBroadcast(
            context,
            phoneNumber.hashCode() + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createBlockPendingIntent(phoneNumber: String): PendingIntent {
        val intent = Intent(context, ActionReceiver::class.java).apply {
            action = ACTION_BLOCK
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
        }
        return PendingIntent.getBroadcast(
            context,
            phoneNumber.hashCode() + 2000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createSendSmsPendingIntent(phoneNumber: String): PendingIntent {
        val intent = Intent(context, ActionReceiver::class.java).apply {
            action = ACTION_SEND_SMS
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
        }
        return PendingIntent.getBroadcast(
            context,
            phoneNumber.hashCode() + 3000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createDismissPendingIntent(phoneNumber: String): PendingIntent {
        val intent = Intent(context, ActionReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
        }
        return PendingIntent.getBroadcast(
            context,
            phoneNumber.hashCode() + 4000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_REJECTED = "rejected_calls"
        const val CHANNEL_SPAM = "spam_detected"
        const val CHANNEL_SMS = "sms_sent"

        const val EXTRA_PHONE_NUMBER = "phone_number"

        const val ACTION_ALLOW = "com.callfilter.ACTION_ALLOW"
        const val ACTION_BLOCK = "com.callfilter.ACTION_BLOCK"
        const val ACTION_SEND_SMS = "com.callfilter.ACTION_SEND_SMS"
        const val ACTION_DISMISS = "com.callfilter.ACTION_DISMISS"

        private const val SMS_CONFIRMATION_ID_BASE = 100000
        private const val SMS_SENT_ID_BASE = 200000
    }
}
