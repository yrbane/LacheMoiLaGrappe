package fr.lachemoilagrappe.domain.repository

import kotlinx.coroutines.flow.Flow

enum class SmsMode {
    MANUAL,      // Prépare le SMS dans l'appli SMS (Default)
    AUTOMATIC    // Envoie automatiquement (nécessite permission SEND_SMS)
}

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
    val phishingProtectionEnabled: Flow<Boolean>
    val sleepModeEnabled: Flow<Boolean>
    val sleepModeStartTime: Flow<String>
    val sleepModeEndTime: Flow<String>
    val allowFixedLinesEnabled: Flow<Boolean>
    val allowLocalAreaCodeEnabled: Flow<Boolean>
    val localAreaCodePrefix: Flow<String>
    val autoSmsMode: Flow<SmsMode>

    suspend fun setFilterUnknownEnabled(enabled: Boolean)
    suspend fun setAutoSmsEnabled(enabled: Boolean)
    suspend fun setSmsConfirmationMode(enabled: Boolean)
    suspend fun setSmsCooldownHours(hours: Int)
    suspend fun setSmsTemplate(template: String)
    suspend fun setBlockTelemarketersEnabled(enabled: Boolean)
    suspend fun setBlockHiddenNumbersEnabled(enabled: Boolean)
    suspend fun setPhishingProtectionEnabled(enabled: Boolean)
    suspend fun setSleepModeEnabled(enabled: Boolean)
    suspend fun setSleepModeStartTime(time: String)
    suspend fun setSleepModeEndTime(time: String)
    suspend fun setAllowFixedLinesEnabled(enabled: Boolean)
    suspend fun setAllowLocalAreaCodeEnabled(enabled: Boolean)
    suspend fun setLocalAreaCodePrefix(prefix: String)
    suspend fun setAutoSmsMode(mode: SmsMode)

    suspend fun getFilterUnknownEnabled(): Boolean
    suspend fun getAutoSmsEnabled(): Boolean
    suspend fun getSmsConfirmationMode(): Boolean
    suspend fun getSmsCooldownHours(): Int
    suspend fun getSmsTemplate(): String
    suspend fun getBlockTelemarketersEnabled(): Boolean
    suspend fun getBlockHiddenNumbersEnabled(): Boolean
    suspend fun getPhishingProtectionEnabled(): Boolean
    suspend fun getSleepModeEnabled(): Boolean
    suspend fun getSleepModeStartTime(): String
    suspend fun getSleepModeEndTime(): String
    suspend fun getAllowFixedLinesEnabled(): Boolean
    suspend fun getAllowLocalAreaCodeEnabled(): Boolean
    suspend fun getLocalAreaCodePrefix(): String
    suspend fun getAutoSmsMode(): SmsMode

    suspend fun getCustomTelemarketerPrefixes(): Set<String>
    suspend fun addCustomTelemarketerPrefix(prefix: String)
    suspend fun removeCustomTelemarketerPrefix(prefix: String)
    suspend fun setCustomTelemarketerPrefixes(prefixes: Set<String>)

    suspend fun getOnboardingCompleted(): Boolean
    suspend fun setOnboardingCompleted(completed: Boolean)
}
