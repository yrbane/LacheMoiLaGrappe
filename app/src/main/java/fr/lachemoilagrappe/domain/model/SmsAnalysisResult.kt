package fr.lachemoilagrappe.domain.model

data class SmsAnalysisResult(
    val isPhishing: Boolean,
    val matchedKeyword: String? = null,
    val hasSuspiciousLink: Boolean = false
)
