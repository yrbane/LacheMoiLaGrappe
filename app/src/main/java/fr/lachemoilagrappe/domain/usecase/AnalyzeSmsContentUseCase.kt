package fr.lachemoilagrappe.domain.usecase

import fr.lachemoilagrappe.domain.model.SmsAnalysisResult
import java.util.Locale
import javax.inject.Inject

class AnalyzeSmsContentUseCase @Inject constructor() {

    companion object {
        private val PHISHING_KEYWORDS = listOf(
            "cpf",
            "amende",
            "colis",
            "chronopost",
            "antai",
            "carte vitale",
            "renouvellement",
            "compte bloqué"
        )
    }

    operator fun invoke(messageBody: String): SmsAnalysisResult {
        val lowerCaseBody = messageBody.lowercase(Locale.getDefault())

        for (keyword in PHISHING_KEYWORDS) {
            if (lowerCaseBody.contains(keyword)) {
                return SmsAnalysisResult(
                    isPhishing = true,
                    matchedKeyword = keyword,
                    hasSuspiciousLink = false // Basic implementation for now
                )
            }
        }

        return SmsAnalysisResult(
            isPhishing = false,
            matchedKeyword = null,
            hasSuspiciousLink = false
        )
    }
}
