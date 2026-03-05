package fr.lachemoilagrappe.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.lachemoilagrappe.R
import fr.lachemoilagrappe.domain.model.CallAction
import fr.lachemoilagrappe.ui.MainActivity
import fr.lachemoilagrappe.util.PhoneNumberHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

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

    /**
     * Generate a safe notification/PendingIntent ID from a phone number.
     * Uses absolute value and modulo to avoid overflow.
     */
    private fun safeNotificationId(phoneNumber: String, offset: Int = 0): Int {
        val baseId = abs(phoneNumber.hashCode()) % MAX_NOTIFICATION_ID
        return baseId + offset
    }

    private var rejectedCountToday = 0

    fun showRejectedCallNotification(phoneNumber: String, action: CallAction) {
        val displayNumber = phoneNumberHelper.formatForDisplay(phoneNumber)
        rejectedCountToday++

        val (channelId, title, text) = when (action) {
            is CallAction.RejectAsSpam -> Triple(
                CHANNEL_SPAM,
                context.getString(R.string.notification_spam_title),
                "Spam détecté : ${action.tag} (score ${action.score}) - $displayNumber"
            )
            is CallAction.RejectAsTelemarketer -> Triple(
                CHANNEL_SPAM,
                "Démarcheur bloqué",
                "Numéro démarcheur rejeté : $displayNumber"
            )
            is CallAction.RejectAsHidden -> Triple(
                CHANNEL_REJECTED,
                "Numéro masqué bloqué",
                "Appel masqué rejeté"
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

        val notificationId = safeNotificationId(phoneNumber, OFFSET_MAIN)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
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
            .setGroup(GROUP_REJECTED)
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
            notificationManager.notify(notificationId, notification)
            // Update the summary notification
            if (rejectedCountToday > 1) {
                showSummaryNotification()
            }
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied for notification")
        }
    }

    private fun showSummaryNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            SUMMARY_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val summary = NotificationCompat.Builder(context, CHANNEL_REJECTED)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("LacheMoiLaGrappe")
            .setContentText("$rejectedCountToday appels bloqués aujourd'hui")
            .setStyle(NotificationCompat.InboxStyle()
                .setSummaryText("$rejectedCountToday appels bloqués"))
            .setGroup(GROUP_REJECTED)
            .setGroupSummary(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(SUMMARY_NOTIFICATION_ID, summary)
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied for summary notification")
        }
    }

    fun showSmsConfirmationNotification(phoneNumber: String) {
        val displayNumber = phoneNumberHelper.formatForDisplay(phoneNumber)
        val notificationId = safeNotificationId(phoneNumber, OFFSET_SMS_CONFIRMATION)

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
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied for SMS confirmation notification")
        }
    }

    fun showSmsSentNotification(phoneNumber: String, success: Boolean) {
        val displayNumber = phoneNumberHelper.formatForDisplay(phoneNumber)
        val text = if (success) {
            "SMS envoyé à $displayNumber"
        } else {
            "Échec d'envoi du SMS à $displayNumber"
        }

        val notificationId = safeNotificationId(phoneNumber, OFFSET_SMS_SENT)
        val notification = NotificationCompat.Builder(context, CHANNEL_SMS)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("SMS")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied for SMS sent notification")
        }
    }

    private fun createAllowPendingIntent(phoneNumber: String): PendingIntent {
        val intent = Intent(context, ActionReceiver::class.java).apply {
            action = ACTION_ALLOW
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
        }
        return PendingIntent.getBroadcast(
            context,
            safeNotificationId(phoneNumber, OFFSET_ACTION_ALLOW),
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
            safeNotificationId(phoneNumber, OFFSET_ACTION_BLOCK),
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
            safeNotificationId(phoneNumber, OFFSET_ACTION_SEND_SMS),
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
            safeNotificationId(phoneNumber, OFFSET_ACTION_DISMISS),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_REJECTED = "rejected_calls"
        const val CHANNEL_SPAM = "spam_detected"
        const val CHANNEL_SMS = "sms_sent"

        const val EXTRA_PHONE_NUMBER = "phone_number"

        const val ACTION_ALLOW = "fr.lachemoilagrappe.ACTION_ALLOW"
        const val ACTION_BLOCK = "fr.lachemoilagrappe.ACTION_BLOCK"
        const val ACTION_SEND_SMS = "fr.lachemoilagrappe.ACTION_SEND_SMS"
        const val ACTION_DISMISS = "fr.lachemoilagrappe.ACTION_DISMISS"

        private const val GROUP_REJECTED = "fr.lachemoilagrappe.REJECTED_GROUP"
        private const val SUMMARY_NOTIFICATION_ID = 0

        // Max ID to prevent overflow (leave room for offsets)
        private const val MAX_NOTIFICATION_ID = 10_000_000

        // Offsets for different notification/action types
        private const val OFFSET_MAIN = 0
        private const val OFFSET_SMS_CONFIRMATION = 1_000_000
        private const val OFFSET_SMS_SENT = 2_000_000
        private const val OFFSET_ACTION_ALLOW = 3_000_000
        private const val OFFSET_ACTION_BLOCK = 4_000_000
        private const val OFFSET_ACTION_SEND_SMS = 5_000_000
        private const val OFFSET_ACTION_DISMISS = 6_000_000
    }
}
