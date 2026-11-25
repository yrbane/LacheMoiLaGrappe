package fr.lachemoilagrappe.domain.usecase

import fr.lachemoilagrappe.domain.model.SmsAnalysisResult
import java.util.Locale
import javax.inject.Inject

class AnalyzeSmsContentUseCase @Inject constructor() {

    companion object {
        // Mots-clés d'arnaques explicites (déclenchent toujours une alerte)
        private val HIGH_RISK_KEYWORDS = listOf(
            "cpf",
            "amende",
            "antai",
            "carte vitale",
            "compte bloqué"
        )

        // Mots-clés courants ("colis", "chronopost", "renouvellement") qui nécessitent un lien ou un indice d'urgence/paiement
        private val CONTEXTUAL_KEYWORDS = listOf(
            "colis",
            "chronopost",
            "renouvellement"
        )

        private val URGENCY_OR_PAYMENT_HINTS = listOf(
            "frais",
            "douane",
            "bloqué",
            "expir",
            "cliquez",
            "réglez",
            "reglez",
            "payer",
            "payez",
            "urgent",
            "suspendu"
        )

        private val URL_REGEX = Regex(
            """(?i)\b(?:https?://|www\.|[a-z0-9.-]+\.(?:com|fr|net|org|xyz|info|top|club|online|site|click|link|ly|gl|co|me|io)\b)\S*"""
        )
    }

    operator fun invoke(messageBody: String): SmsAnalysisResult {
        val lowerCaseBody = messageBody.lowercase(Locale.getDefault())
        val hasLink = URL_REGEX.containsMatchIn(lowerCaseBody)

        for (keyword in HIGH_RISK_KEYWORDS) {
            if (lowerCaseBody.contains(keyword)) {
                return SmsAnalysisResult(
                    isPhishing = true,
                    matchedKeyword = keyword,
                    hasSuspiciousLink = hasLink
                )
            }
        }

        val hasUrgencyHint = URGENCY_OR_PAYMENT_HINTS.any { lowerCaseBody.contains(it) }
        if (hasLink || hasUrgencyHint) {
            for (keyword in CONTEXTUAL_KEYWORDS) {
                if (lowerCaseBody.contains(keyword)) {
                    return SmsAnalysisResult(
                        isPhishing = true,
                        matchedKeyword = keyword,
                        hasSuspiciousLink = hasLink
                    )
                }
            }
        }

        return SmsAnalysisResult(
            isPhishing = false,
            matchedKeyword = null,
            hasSuspiciousLink = hasLink
        )
    }
}
