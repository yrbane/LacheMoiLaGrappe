package com.callfilter.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.callfilter.domain.model.CallAction
import com.callfilter.domain.model.SmsDecision
import com.callfilter.domain.usecase.DecideCallActionUseCase
import com.callfilter.domain.usecase.LogCallEventUseCase
import com.callfilter.domain.usecase.SendIdentitySmsUseCase
import com.callfilter.domain.usecase.ShouldSendSmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallFilterScreeningService : CallScreeningService() {

    @Inject
    lateinit var decideCallActionUseCase: DecideCallActionUseCase

    @Inject
    lateinit var logCallEventUseCase: LogCallEventUseCase

    @Inject
    lateinit var shouldSendSmsUseCase: ShouldSendSmsUseCase

    @Inject
    lateinit var sendIdentitySmsUseCase: SendIdentitySmsUseCase

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart

        if (phoneNumber.isNullOrBlank()) {
            // Numéro masqué - laisser passer par défaut
            respondToCall(callDetails, createAllowResponse())
            return
        }

        serviceScope.launch {
            try {
                val action = decideCallActionUseCase(phoneNumber)
                val response = createResponse(action)

                respondToCall(callDetails, response)

                // Logger l'événement
                logCallEventUseCase(phoneNumber, action)

                // Gérer les actions post-rejet
                if (action != CallAction.Allow) {
                    handlePostReject(phoneNumber, action)
                }
            } catch (e: Exception) {
                // En cas d'erreur, laisser passer l'appel
                respondToCall(callDetails, createAllowResponse())
            }
        }
    }

    private fun createResponse(action: CallAction): CallResponse {
        return CallResponse.Builder().apply {
            when (action) {
                is CallAction.Allow -> {
                    setDisallowCall(false)
                    setRejectCall(false)
                    setSkipNotification(false)
                    setSkipCallLog(false)
                }
                is CallAction.Reject,
                is CallAction.RejectAsSpam,
                is CallAction.Block -> {
                    setDisallowCall(true)
                    setRejectCall(true)
                    setSkipNotification(false)
                    setSkipCallLog(false)
                }
            }
        }.build()
    }

    private fun createAllowResponse(): CallResponse {
        return CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipNotification(false)
            .setSkipCallLog(false)
            .build()
    }

    private suspend fun handlePostReject(phoneNumber: String, action: CallAction) {
        // Afficher une notification
        notificationHelper.showRejectedCallNotification(phoneNumber, action)

        // Vérifier si on doit envoyer un SMS
        if (action is CallAction.Reject) {
            when (val smsDecision = shouldSendSmsUseCase(phoneNumber)) {
                is SmsDecision.Send -> {
                    val result = sendIdentitySmsUseCase(phoneNumber)
                    notificationHelper.showSmsSentNotification(phoneNumber, result.isSuccess)
                }
                is SmsDecision.AskConfirmation -> {
                    notificationHelper.showSmsConfirmationNotification(phoneNumber)
                }
                is SmsDecision.Skip -> {
                    // Ne rien faire
                }
            }
        }
    }
}
