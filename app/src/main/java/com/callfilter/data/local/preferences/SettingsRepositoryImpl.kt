package com.callfilter.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.callfilter.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val FILTER_UNKNOWN_ENABLED = booleanPreferencesKey("filter_unknown_enabled")
        val SPAM_DB_ENABLED = booleanPreferencesKey("spam_db_enabled")
        val AUTO_SMS_ENABLED = booleanPreferencesKey("auto_sms_enabled")
        val SMS_CONFIRMATION_MODE = booleanPreferencesKey("sms_confirmation_mode")
        val SMS_COOLDOWN_HOURS = intPreferencesKey("sms_cooldown_hours")
        val SMS_TEMPLATE = stringPreferencesKey("sms_template")
    }

    private object Defaults {
        const val FILTER_UNKNOWN_ENABLED = true
        const val SPAM_DB_ENABLED = true
        const val AUTO_SMS_ENABLED = false
        const val SMS_CONFIRMATION_MODE = true
        const val SMS_COOLDOWN_HOURS = 24
        const val SMS_TEMPLATE = "Bonjour, je filtre les appels inconnus. Pouvez-vous m'indiquer votre identité et l'objet de votre appel ? Merci."
    }

    override val filterUnknownEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.FILTER_UNKNOWN_ENABLED] ?: Defaults.FILTER_UNKNOWN_ENABLED }

    override val spamDbEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.SPAM_DB_ENABLED] ?: Defaults.SPAM_DB_ENABLED }

    override val autoSmsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.AUTO_SMS_ENABLED] ?: Defaults.AUTO_SMS_ENABLED }

    override val smsConfirmationMode: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.SMS_CONFIRMATION_MODE] ?: Defaults.SMS_CONFIRMATION_MODE }

    override val smsCooldownHours: Flow<Int> = context.dataStore.data
        .map { it[Keys.SMS_COOLDOWN_HOURS] ?: Defaults.SMS_COOLDOWN_HOURS }

    override val smsTemplate: Flow<String> = context.dataStore.data
        .map { it[Keys.SMS_TEMPLATE] ?: Defaults.SMS_TEMPLATE }

    override suspend fun setFilterUnknownEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FILTER_UNKNOWN_ENABLED] = enabled }
    }

    override suspend fun setSpamDbEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SPAM_DB_ENABLED] = enabled }
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

    override suspend fun getFilterUnknownEnabled(): Boolean =
        filterUnknownEnabled.first()

    override suspend fun getSpamDbEnabled(): Boolean =
        spamDbEnabled.first()

    override suspend fun getAutoSmsEnabled(): Boolean =
        autoSmsEnabled.first()

    override suspend fun getSmsConfirmationMode(): Boolean =
        smsConfirmationMode.first()

    override suspend fun getSmsCooldownHours(): Int =
        smsCooldownHours.first()

    override suspend fun getSmsTemplate(): String =
        smsTemplate.first()
}
