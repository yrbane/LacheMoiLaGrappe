package fr.lachemoilagrappe.domain.model

enum class CallDecision {
    ALLOWED,
    REJECTED,
    REJECTED_SPAM,
    REJECTED_TELEMARKETER,
    REJECTED_HIDDEN,
    BLOCKED
}
