package com.callfilter.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val filterUnknownEnabled: Flow<Boolean>
    val spamDbEnabled: Flow<Boolean>
    val autoSmsEnabled: Flow<Boolean>
    val smsConfirmationMode: Flow<Boolean>
    val smsCooldownHours: Flow<Int>
    val smsTemplate: Flow<String>

    suspend fun setFilterUnknownEnabled(enabled: Boolean)
    suspend fun setSpamDbEnabled(enabled: Boolean)
    suspend fun setAutoSmsEnabled(enabled: Boolean)
    suspend fun setSmsConfirmationMode(enabled: Boolean)
    suspend fun setSmsCooldownHours(hours: Int)
    suspend fun setSmsTemplate(template: String)

    suspend fun getFilterUnknownEnabled(): Boolean
    suspend fun getSpamDbEnabled(): Boolean
    suspend fun getAutoSmsEnabled(): Boolean
    suspend fun getSmsConfirmationMode(): Boolean
    suspend fun getSmsCooldownHours(): Int
    suspend fun getSmsTemplate(): String
}
