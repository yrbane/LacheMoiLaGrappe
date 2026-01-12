package com.callfilter.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callfilter.domain.model.CallDecision
import com.callfilter.domain.repository.CallLogRepository
import com.callfilter.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class HomeUiState(
    val filterUnknownEnabled: Boolean = true,
    val spamDbEnabled: Boolean = true,
    val autoSmsEnabled: Boolean = false,
    val todayRejectedCount: Int = 0,
    val todaySpamCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    private val _todayRejectedCount = MutableStateFlow(0)
    private val _todaySpamCount = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.filterUnknownEnabled,
        settingsRepository.spamDbEnabled,
        settingsRepository.autoSmsEnabled,
        _todayRejectedCount,
        _todaySpamCount
    ) { filterEnabled, spamEnabled, smsEnabled, rejected, spam ->
        HomeUiState(
            filterUnknownEnabled = filterEnabled,
            spamDbEnabled = spamEnabled,
            autoSmsEnabled = smsEnabled,
            todayRejectedCount = rejected,
            todaySpamCount = spam,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        loadTodayStats()
    }

    private fun loadTodayStats() {
        viewModelScope.launch {
            val todayStart = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
            // Simplified - would need actual implementation
            _todayRejectedCount.value = callLogRepository.getCallCountSince(todayStart)
        }
    }

    fun setFilterUnknownEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFilterUnknownEnabled(enabled)
        }
    }

    fun setSpamDbEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSpamDbEnabled(enabled)
        }
    }

    fun setAutoSmsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSmsEnabled(enabled)
        }
    }
}
