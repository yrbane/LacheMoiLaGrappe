package fr.lachemoilagrappe.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val FILTER_UNKNOWN_ENABLED = booleanPreferencesKey("filter_unknown_enabled")
        val AUTO_SMS_ENABLED = booleanPreferencesKey("auto_sms_enabled")
        val SMS_CONFIRMATION_MODE = booleanPreferencesKey("sms_confirmation_mode")
        val SMS_COOLDOWN_HOURS = intPreferencesKey("sms_cooldown_hours")
        val SMS_TEMPLATE = stringPreferencesKey("sms_template")
        val BLOCK_TELEMARKETERS_ENABLED = booleanPreferencesKey("block_telemarketers_enabled")
        val BLOCK_HIDDEN_NUMBERS_ENABLED = booleanPreferencesKey("block_hidden_numbers_enabled")
        val CUSTOM_TELEMARKETER_PREFIXES = stringPreferencesKey("custom_telemarketer_prefixes")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PHISHING_PROTECTION_ENABLED = booleanPreferencesKey("phishing_protection_enabled")
        val SLEEP_MODE_ENABLED = booleanPreferencesKey("sleep_mode_enabled")
        val SLEEP_MODE_START_TIME = stringPreferencesKey("sleep_mode_start_time")
        val SLEEP_MODE_END_TIME = stringPreferencesKey("sleep_mode_end_time")
    }

    private object Defaults {
        const val FILTER_UNKNOWN_ENABLED = true
        const val AUTO_SMS_ENABLED = false
        const val SMS_CONFIRMATION_MODE = true
        const val SMS_COOLDOWN_HOURS = 24
        const val SMS_TEMPLATE = "Bonjour, je filtre les appels inconnus. Pouvez-vous m'indiquer votre identité et l'objet de votre appel ? Merci."
        const val BLOCK_TELEMARKETERS_ENABLED = true
        const val BLOCK_HIDDEN_NUMBERS_ENABLED = true
        const val PHISHING_PROTECTION_ENABLED = true
        const val SLEEP_MODE_ENABLED = false
        const val SLEEP_MODE_START_TIME = "22:00"
        const val SLEEP_MODE_END_TIME = "07:00"
    }

    override val filterUnknownEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.FILTER_UNKNOWN_ENABLED] ?: Defaults.FILTER_UNKNOWN_ENABLED }

    override val autoSmsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.AUTO_SMS_ENABLED] ?: Defaults.AUTO_SMS_ENABLED }

    override val smsConfirmationMode: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.SMS_CONFIRMATION_MODE] ?: Defaults.SMS_CONFIRMATION_MODE }

    override val smsCooldownHours: Flow<Int> = context.dataStore.data
        .map { it[Keys.SMS_COOLDOWN_HOURS] ?: Defaults.SMS_COOLDOWN_HOURS }

    override val smsTemplate: Flow<String> = context.dataStore.data
        .map { it[Keys.SMS_TEMPLATE] ?: Defaults.SMS_TEMPLATE }

    override val blockTelemarketersEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.BLOCK_TELEMARKETERS_ENABLED] ?: Defaults.BLOCK_TELEMARKETERS_ENABLED }

    override val blockHiddenNumbersEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.BLOCK_HIDDEN_NUMBERS_ENABLED] ?: Defaults.BLOCK_HIDDEN_NUMBERS_ENABLED }

    override val customTelemarketerPrefixes: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            val json = preferences[Keys.CUSTOM_TELEMARKETER_PREFIXES] ?: "[]"
            jsonToSet(json)
        }

    override val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.ONBOARDING_COMPLETED] ?: false }
        
    override val phishingProtectionEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.PHISHING_PROTECTION_ENABLED] ?: Defaults.PHISHING_PROTECTION_ENABLED }

    override val sleepModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.SLEEP_MODE_ENABLED] ?: Defaults.SLEEP_MODE_ENABLED }

    override val sleepModeStartTime: Flow<String> = context.dataStore.data
        .map { it[Keys.SLEEP_MODE_START_TIME] ?: Defaults.SLEEP_MODE_START_TIME }

    override val sleepModeEndTime: Flow<String> = context.dataStore.data
        .map { it[Keys.SLEEP_MODE_END_TIME] ?: Defaults.SLEEP_MODE_END_TIME }

    override suspend fun setFilterUnknownEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FILTER_UNKNOWN_ENABLED] = enabled }
    }

    override suspend fun setAutoSmsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SMS_ENABLED] = enabled }
    }

    override suspend fun setSmsConfirmationMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SMS_CONFIRMATION_MODE] = enabled }
    }

    override suspend fun setSmsCooldownHours(hours: Int) {
        context.dataStore.edit { it[Keys.SMS_COOLDOWN_HOURS] = hours }
    }

    override suspend fun setSmsTemplate(template: String) {
        context.dataStore.edit { it[Keys.SMS_TEMPLATE] = template }
    }

    override suspend fun setBlockTelemarketersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BLOCK_TELEMARKETERS_ENABLED] = enabled }
    }

    override suspend fun setBlockHiddenNumbersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BLOCK_HIDDEN_NUMBERS_ENABLED] = enabled }
    }
    
    override suspend fun setPhishingProtectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PHISHING_PROTECTION_ENABLED] = enabled }
    }

    override suspend fun setSleepModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SLEEP_MODE_ENABLED] = enabled }
    }

    override suspend fun setSleepModeStartTime(time: String) {
        context.dataStore.edit { it[Keys.SLEEP_MODE_START_TIME] = time }
    }

    override suspend fun setSleepModeEndTime(time: String) {
        context.dataStore.edit { it[Keys.SLEEP_MODE_END_TIME] = time }
    }

    override suspend fun getFilterUnknownEnabled(): Boolean = filterUnknownEnabled.first()
    override suspend fun getAutoSmsEnabled(): Boolean = autoSmsEnabled.first()
    override suspend fun getSmsConfirmationMode(): Boolean = smsConfirmationMode.first()
    override suspend fun getSmsCooldownHours(): Int = smsCooldownHours.first()
    override suspend fun getSmsTemplate(): String = smsTemplate.first()
    override suspend fun getBlockTelemarketersEnabled(): Boolean = blockTelemarketersEnabled.first()
    override suspend fun getBlockHiddenNumbersEnabled(): Boolean = blockHiddenNumbersEnabled.first()
    override suspend fun getPhishingProtectionEnabled(): Boolean = phishingProtectionEnabled.first()
    override suspend fun getSleepModeEnabled(): Boolean = sleepModeEnabled.first()
    override suspend fun getSleepModeStartTime(): String = sleepModeStartTime.first()
    override suspend fun getSleepModeEndTime(): String = sleepModeEndTime.first()

    override suspend fun getCustomTelemarketerPrefixes(): Set<String> = customTelemarketerPrefixes.first()

    override suspend fun addCustomTelemarketerPrefix(prefix: String) {
        val normalized = normalizePrefix(prefix) ?: return
        if (!isValidPrefix(normalized)) return
        context.dataStore.edit { preferences ->
            val current = jsonToSet(preferences[Keys.CUSTOM_TELEMARKETER_PREFIXES] ?: "[]")
            preferences[Keys.CUSTOM_TELEMARKETER_PREFIXES] = setToJson(current + normalized)
        }
    }

    override suspend fun removeCustomTelemarketerPrefix(prefix: String) {
        context.dataStore.edit { preferences ->
            val current = jsonToSet(preferences[Keys.CUSTOM_TELEMARKETER_PREFIXES] ?: "[]")
            preferences[Keys.CUSTOM_TELEMARKETER_PREFIXES] = setToJson(current - prefix)
        }
    }

    override suspend fun getOnboardingCompleted(): Boolean = onboardingCompleted.first()
    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun setCustomTelemarketerPrefixes(prefixes: Set<String>) {
        val validPrefixes = prefixes.mapNotNull { normalizePrefix(it) }.filter { isValidPrefix(it) }.toSet()
        context.dataStore.edit { it[Keys.CUSTOM_TELEMARKETER_PREFIXES] = setToJson(validPrefixes) }
    }

    private fun normalizePrefix(prefix: String): String? = prefix.replace(Regex("\\s"), "").ifEmpty { null }
    private fun isValidPrefix(prefix: String): Boolean = prefix.length in 4..5 && prefix.all { it.isDigit() }
    private fun jsonToSet(json: String): Set<String> = try {
        val array = JSONArray(json)
        (0 until array.length()).map { array.getString(it) }.toSet()
    } catch (e: Exception) { emptySet() }
    private fun setToJson(set: Set<String>): String {
        val array = JSONArray()
        set.forEach { array.put(it) }
        return array.toString()
    }
}
