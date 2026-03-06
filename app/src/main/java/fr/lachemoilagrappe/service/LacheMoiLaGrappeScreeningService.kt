package fr.lachemoilagrappe.service

import android.telecom.Call
import android.telecom.CallScreeningService
import fr.lachemoilagrappe.domain.model.CallAction
import fr.lachemoilagrappe.domain.model.SmsDecision
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import fr.lachemoilagrappe.domain.usecase.DecideCallActionUseCase
import fr.lachemoilagrappe.domain.usecase.LogCallEventUseCase
import fr.lachemoilagrappe.domain.usecase.SendIdentitySmsUseCase
import fr.lachemoilagrappe.domain.usecase.ShouldSendSmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LacheMoiLaGrappeScreeningService : CallScreeningService() {

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

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Uncaught exception in LacheMoiLaGrappeScreeningService")
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Timber.d("LacheMoiLaGrappeScreeningService destroyed")
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart

        serviceScope.launch {
            try {
                processCall(callDetails, phoneNumber)
            } catch (e: CancellationException) {
                throw e // Don't catch cancellation
            } catch (e: Exception) {
                Timber.e(e, "Error processing call from: ${phoneNumber?.take(4)}***")
                // En cas d'erreur critique, on laisse passer l'appel
                // mais on log l'erreur pour investigation
                safeRespondToCall(callDetails, createAllowResponse())
            }
        }
    }

    private suspend fun processCall(callDetails: Call.Details, phoneNumber: String?) {
        // Numéro masqué
        if (phoneNumber.isNullOrBlank()) {
            handleHiddenNumber(callDetails)
            return
        }

        Timber.d("Processing call from: ${phoneNumber.take(4)}***")

        val action = decideCallActionUseCase(phoneNumber)
        Timber.d("Decision for ${phoneNumber.take(4)}***: $action")

        val response = createResponse(action)
        safeRespondToCall(callDetails, response)

        // Logger l'événement (non-bloquant en cas d'erreur)
        try {
            logCallEventUseCase(phoneNumber, action)
        } catch (e: Exception) {
            Timber.e(e, "Failed to log call event")
        }

        // Gérer les actions post-rejet
        if (action != CallAction.Allow) {
            handlePostReject(phoneNumber, action)
        }
    }

    private suspend fun handleHiddenNumber(callDetails: Call.Details) {
        try {
            val blockHidden = settingsRepository.getBlockHiddenNumbersEnabled()
            Timber.d("Hidden number - block enabled: $blockHidden")

            if (blockHidden) {
                val action = CallAction.RejectAsHidden
                safeRespondToCall(callDetails, createRejectResponse())

                try {
                    logCallEventUseCase("Numéro masqué", action)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to log hidden call event")
                }

                notificationHelper.showRejectedCallNotification("Numéro masqué", action)
            } else {
                safeRespondToCall(callDetails, createAllowResponse())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling hidden number, allowing call")
            safeRespondToCall(callDetails, createAllowResponse())
        }
    }

    private fun safeRespondToCall(callDetails: Call.Details, response: CallResponse) {
        try {
            respondToCall(callDetails, response)
        } catch (e: Exception) {
            Timber.e(e, "Failed to respond to call")
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
                is CallAction.RejectAsTelemarketer,
                is CallAction.RejectAsHidden,
                is CallAction.Block -> {
                    setDisallowCall(true)
                    setRejectCall(true)
                    setSkipNotification(false)
                    setSkipCallLog(false)
                }
            }
        }.build()
    }

    private fun createRejectResponse(): CallResponse {
        return CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipNotification(false)
            .setSkipCallLog(false)
            .build()
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
        // Afficher une notification (non-bloquant en cas d'erreur)
        try {
            notificationHelper.showRejectedCallNotification(phoneNumber, action)
        } catch (e: Exception) {
            Timber.e(e, "Failed to show rejection notification")
        }

        // Vérifier si on doit envoyer un SMS
        if (action is CallAction.Reject) {
            try {
                when (val smsDecision = shouldSendSmsUseCase(phoneNumber)) {
                    is SmsDecision.Send -> {
                        val result = sendIdentitySmsUseCase(phoneNumber)
                        notificationHelper.showSmsSentNotification(phoneNumber, result.isSuccess)
                        Timber.d("SMS sent to ${phoneNumber.take(4)}***: ${result.isSuccess}")
                    }
                    is SmsDecision.AskConfirmation -> {
                        notificationHelper.showSmsConfirmationNotification(phoneNumber)
                        Timber.d("SMS confirmation requested for ${phoneNumber.take(4)}***")
                    }
                    is SmsDecision.Skip -> {
                        Timber.d("SMS skipped for ${phoneNumber.take(4)}***: ${smsDecision.reason}")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling SMS decision")
            }
        }
    }
}
