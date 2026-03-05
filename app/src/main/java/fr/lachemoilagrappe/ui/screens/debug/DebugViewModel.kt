package fr.lachemoilagrappe.ui.screens.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.lachemoilagrappe.domain.model.CallAction
import fr.lachemoilagrappe.domain.model.SmsDecision
import fr.lachemoilagrappe.domain.repository.ContactsRepository
import fr.lachemoilagrappe.domain.usecase.DecideCallActionUseCase
import fr.lachemoilagrappe.domain.usecase.LogCallEventUseCase
import fr.lachemoilagrappe.domain.usecase.SendIdentitySmsUseCase
import fr.lachemoilagrappe.domain.usecase.ShouldSendSmsUseCase
import fr.lachemoilagrappe.service.NotificationHelper
import fr.lachemoilagrappe.util.PhoneNumberHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DebugUiState(
    val lastResult: TestResult? = null,
    val lastSmsResult: SmsTestResult? = null,
    val logs: List<String> = emptyList(),
    val isLoading: Boolean = false
)

data class SmsTestResult(
    val phoneNumber: String,
    val decision: SmsDecision,
    val decisionText: String,
    val smsSent: Boolean = false,
    val smsId: Long? = null,
    val error: String? = null
)

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val decideCallActionUseCase: DecideCallActionUseCase,
    private val logCallEventUseCase: LogCallEventUseCase,
    private val shouldSendSmsUseCase: ShouldSendSmsUseCase,
    private val sendIdentitySmsUseCase: SendIdentitySmsUseCase,
    private val contactsRepository: ContactsRepository,
    private val phoneNumberHelper: PhoneNumberHelper,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebugUiState())
    val uiState: StateFlow<DebugUiState> = _uiState.asStateFlow()

    fun testCall(phoneNumber: String) {
        viewModelScope.launch {
            addLog("=== Test pour: $phoneNumber ===")

            try {
                // Normaliser le numéro
                val normalized = phoneNumberHelper.normalize(phoneNumber)
                addLog("Numéro normalisé: $normalized")

                // Vérifier si dans contacts
                val isInContacts = contactsRepository.isNumberInContacts(phoneNumber)
                addLog("Dans contacts: $isInContacts")

                // Vérifier si mobile
                val isMobile = phoneNumberHelper.isMobileNumber(phoneNumber)
                addLog("Est mobile: $isMobile")

                // Décision
                val action = decideCallActionUseCase(phoneNumber)
                addLog("Décision: $action")

                val (actionName, reason) = when (action) {
                    is CallAction.Allow -> "AUTORISÉ" to "L'appel serait accepté"
                    is CallAction.Reject -> "REJETÉ" to "Numéro inconnu - appel rejeté"
                    is CallAction.RejectAsSpam -> "SPAM" to "Spam détecté: ${action.tag} (score: ${action.score})"
                    is CallAction.RejectAsTelemarketer -> "DÉMARCHEUR" to "Préfixe réservé aux démarcheurs (ARCEP)"
                    is CallAction.RejectAsHidden -> "MASQUÉ" to "Numéro masqué - appel rejeté"
                    is CallAction.Block -> "BLOQUÉ" to "Numéro dans la blocklist"
                }

                _uiState.update {
                    it.copy(
                        lastResult = TestResult(
                            phoneNumber = phoneNumber,
                            normalizedNumber = normalized ?: "N/A",
                            action = action,
                            actionName = actionName,
                            reason = reason,
                            isInContacts = isInContacts,
                            isMobile = isMobile
                        )
                    )
                }

                addLog("Résultat: $actionName")
            } catch (e: Exception) {
                addLog("ERREUR: ${e.message}")
            }
        }
    }

    fun simulateFullCall(phoneNumber: String) {
        viewModelScope.launch {
            addLog("=== Simulation complète pour: $phoneNumber ===")

            try {
                // Décision
                val action = decideCallActionUseCase(phoneNumber)
                addLog("Décision: $action")

                // Logger l'appel
                val logId = logCallEventUseCase(phoneNumber, action)
                addLog("Appel loggé avec ID: $logId")

                // Notification si rejeté
                if (action != CallAction.Allow) {
                    notificationHelper.showRejectedCallNotification(phoneNumber, action)
                    addLog("Notification envoyée")
                }

                // Mettre à jour le résultat
                testCall(phoneNumber)

                addLog("Simulation terminée - Vérifiez l'historique et les notifications")
            } catch (e: Exception) {
                addLog("ERREUR: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList(), lastResult = null, lastSmsResult = null) }
    }

    fun testSms(phoneNumber: String) {
        viewModelScope.launch {
            addLog("=== Test SMS pour: $phoneNumber ===")

            try {
                val decision = shouldSendSmsUseCase(phoneNumber)
                addLog("Décision SMS: $decision")

                val decisionText = when (decision) {
                    is SmsDecision.Send -> "SMS serait envoyé"
                    is SmsDecision.AskConfirmation -> "Confirmation demandée"
                    is SmsDecision.Skip -> "Ignoré: ${decision.reason}"
                }

                _uiState.update {
                    it.copy(
                        lastSmsResult = SmsTestResult(
                            phoneNumber = phoneNumber,
                            decision = decision,
                            decisionText = decisionText
                        )
                    )
                }

                addLog("Résultat: $decisionText")
            } catch (e: Exception) {
                addLog("ERREUR SMS: ${e.message}")
                _uiState.update {
                    it.copy(
                        lastSmsResult = SmsTestResult(
                            phoneNumber = phoneNumber,
                            decision = SmsDecision.Skip("Erreur"),
                            decisionText = "Erreur",
                            error = e.message
                        )
                    )
                }
            }
        }
    }

    fun sendTestSms(phoneNumber: String) {
        viewModelScope.launch {
            addLog("=== Envoi SMS réel à: $phoneNumber ===")

            try {
                val result = sendIdentitySmsUseCase(phoneNumber)
                result.fold(
                    onSuccess = { smsId ->
                        addLog("SMS envoyé avec succès! ID: $smsId")
                        _uiState.update {
                            it.copy(
                                lastSmsResult = SmsTestResult(
                                    phoneNumber = phoneNumber,
                                    decision = SmsDecision.Send,
                                    decisionText = "SMS envoyé",
                                    smsSent = true,
                                    smsId = smsId
                                )
                            )
                        }
                    },
                    onFailure = { error ->
                        addLog("ERREUR envoi SMS: ${error.message}")
                        _uiState.update {
                            it.copy(
                                lastSmsResult = SmsTestResult(
                                    phoneNumber = phoneNumber,
                                    decision = SmsDecision.Skip("Échec envoi"),
                                    decisionText = "Échec envoi",
                                    error = error.message
                                )
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                addLog("ERREUR: ${e.message}")
            }
        }
    }

    private fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        _uiState.update {
            it.copy(logs = it.logs + "[$timestamp] $message")
        }
    }
}
