package com.callfilter.domain.model

sealed class SmsDecision {
    data object Send : SmsDecision()
    data object AskConfirmation : SmsDecision()
    data class Skip(val reason: String) : SmsDecision()
}
