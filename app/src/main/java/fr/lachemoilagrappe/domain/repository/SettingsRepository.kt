package fr.lachemoilagrappe.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val filterUnknownEnabled: Flow<Boolean>
    val autoSmsEnabled: Flow<Boolean>
    val smsConfirmationMode: Flow<Boolean>
    val smsCooldownHours: Flow<Int>
    val smsTemplate: Flow<String>
    val blockTelemarketersEnabled: Flow<Boolean>
    val blockHiddenNumbersEnabled: Flow<Boolean>
    val customTelemarketerPrefixes: Flow<Set<String>>
    val onboardingCompleted: Flow<Boolean>

    suspend fun setFilterUnknownEnabled(enabled: Boolean)
    suspend fun setAutoSmsEnabled(enabled: Boolean)
    suspend fun setSmsConfirmationMode(enabled: Boolean)
    suspend fun setSmsCooldownHours(hours: Int)
    suspend fun setSmsTemplate(template: String)
    suspend fun setBlockTelemarketersEnabled(enabled: Boolean)
    suspend fun setBlockHiddenNumbersEnabled(enabled: Boolean)

    suspend fun getFilterUnknownEnabled(): Boolean
    suspend fun getAutoSmsEnabled(): Boolean
    suspend fun getSmsConfirmationMode(): Boolean
    suspend fun getSmsCooldownHours(): Int
    suspend fun getSmsTemplate(): String
    suspend fun getBlockTelemarketersEnabled(): Boolean
    suspend fun getBlockHiddenNumbersEnabled(): Boolean

    suspend fun getCustomTelemarketerPrefixes(): Set<String>
    suspend fun addCustomTelemarketerPrefix(prefix: String)
    suspend fun removeCustomTelemarketerPrefix(prefix: String)
    suspend fun setCustomTelemarketerPrefixes(prefixes: Set<String>)

    suspend fun getOnboardingCompleted(): Boolean
    suspend fun setOnboardingCompleted(completed: Boolean)
}
