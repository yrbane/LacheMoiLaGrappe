package fr.lachemoilagrappe.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    fun setFilterTelemarketers(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBlockTelemarketersEnabled(enabled)
        }
    }

    fun setAutoSms(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSmsEnabled(enabled)
        }
    }

    fun setPhishingProtection(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPhishingProtectionEnabled(enabled)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
        }
    }
}
