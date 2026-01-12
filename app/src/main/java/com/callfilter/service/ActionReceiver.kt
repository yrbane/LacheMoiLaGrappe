package com.callfilter.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.callfilter.domain.repository.UserListRepository
import com.callfilter.domain.usecase.SendIdentitySmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userListRepository: UserListRepository

    @Inject
    lateinit var sendIdentitySmsUseCase: SendIdentitySmsUseCase

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val phoneNumber = intent.getStringExtra(NotificationHelper.EXTRA_PHONE_NUMBER) ?: return
        val notificationManager = NotificationManagerCompat.from(context)

        // Annuler la notification
        notificationManager.cancel(phoneNumber.hashCode())

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
