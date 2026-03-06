package fr.lachemoilagrappe.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import fr.lachemoilagrappe.domain.usecase.DecideCallActionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val filterUnknownEnabled: Boolean = true,
    val blockTelemarketersEnabled: Boolean = true,
    val blockHiddenNumbersEnabled: Boolean = true,
    val autoSmsEnabled: Boolean = false,
    val smsConfirmationMode: Boolean = true,
    val smsCooldownHours: Int = 24,
    val smsTemplate: String = "",
    val customTelemarketerPrefixes: Set<String> = emptySet(),
    val arcepPrefixes: Set<String> = DecideCallActionUseCase.ARCEP_PREFIXES,
    val phishingProtectionEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.filterUnknownEnabled,
        settingsRepository.blockTelemarketersEnabled,
        settingsRepository.blockHiddenNumbersEnabled,
        settingsRepository.autoSmsEnabled,
        settingsRepository.smsConfirmationMode,
        settingsRepository.smsCooldownHours,
        settingsRepository.smsTemplate,
        settingsRepository.customTelemarketerPrefixes,
        settingsRepository.phishingProtectionEnabled
    ) { values ->
        SettingsUiState(
            filterUnknownEnabled = values[0] as Boolean,
            blockTelemarketersEnabled = values[1] as Boolean,
            blockHiddenNumbersEnabled = values[2] as Boolean,
            autoSmsEnabled = values[3] as Boolean,
            smsConfirmationMode = values[4] as Boolean,
            smsCooldownHours = values[5] as Int,
            smsTemplate = values[6] as String,
            customTelemarketerPrefixes = values[7] as Set<String>,
            phishingProtectionEnabled = values[8] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setPhishingProtectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPhishingProtectionEnabled(enabled)
        }
    }

    fun setFilterUnknownEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFilterUnknownEnabled(enabled)
        }
    }

    fun setBlockTelemarketersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBlockTelemarketersEnabled(enabled)
        }
    }

    fun setBlockHiddenNumbersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBlockHiddenNumbersEnabled(enabled)
        }
    }

    fun setAutoSmsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSmsEnabled(enabled)
        }
    }

    fun setSmsConfirmationMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSmsConfirmationMode(enabled)
        }
    }

    fun setSmsCooldownHours(hours: Int) {
        viewModelScope.launch {
            settingsRepository.setSmsCooldownHours(hours.coerceIn(1, 72))
        }
    }

    fun setSmsTemplate(template: String) {
        viewModelScope.launch {
            settingsRepository.setSmsTemplate(template)
        }
    }

    fun addCustomPrefix(prefix: String) {
        viewModelScope.launch {
            settingsRepository.addCustomTelemarketerPrefix(prefix)
        }
    }

    fun removeCustomPrefix(prefix: String) {
        viewModelScope.launch {
            settingsRepository.removeCustomTelemarketerPrefix(prefix)
        }
    }

    /**
     * Validate if a prefix is valid for adding.
     * Returns error message or null if valid.
     */
    fun validatePrefix(prefix: String): String? {
        val normalized = prefix.replace(Regex("\\s"), "")
        return when {
            normalized.isEmpty() -> "Le préfixe ne peut pas être vide"
            normalized.length < 4 -> "Le préfixe doit contenir au moins 4 chiffres"
            normalized.length > 5 -> "Le préfixe ne peut pas dépasser 5 chiffres"
            !normalized.all { it.isDigit() } -> "Le préfixe ne doit contenir que des chiffres"
            uiState.value.arcepPrefixes.contains(normalized) -> "Ce préfixe est déjà dans la liste ARCEP"
            uiState.value.customTelemarketerPrefixes.contains(normalized) -> "Ce préfixe existe déjà"
            else -> null
        }
    }
}
