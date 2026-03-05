package fr.lachemoilagrappe.domain.model

sealed class CallAction {
    data object Allow : CallAction()
    data object Reject : CallAction()
    data class RejectAsSpam(val tag: String, val score: Int) : CallAction()
    data object RejectAsTelemarketer : CallAction()
    data object RejectAsHidden : CallAction()
    data object Block : CallAction()
}
