package fr.lachemoilagrappe.domain.model

sealed class CallAction {
    data object Allow : CallAction()
    data object Reject : CallAction()
    data object RejectAsTelemarketer : CallAction()
    data object RejectAsHidden : CallAction()
    data object Block : CallAction()
}
