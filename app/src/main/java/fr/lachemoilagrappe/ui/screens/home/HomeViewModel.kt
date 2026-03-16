package fr.lachemoilagrappe.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.lachemoilagrappe.domain.repository.CallLogRepository
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val filterUnknownEnabled: Boolean = true,
    val autoSmsEnabled: Boolean = false,
    val todayRejectedCount: Int = 0,
    val totalBlockedCount: Int = 0,
    val blockedStats: Map<Long, Int> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    private val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.filterUnknownEnabled,
        settingsRepository.autoSmsEnabled,
        callLogRepository.getBlockedCountSinceFlow(todayStart),
        callLogRepository.getTotalBlockedCountFlow(),
        callLogRepository.getBlockedStatsLastDays(7)
    ) { filterEnabled, smsEnabled, todayRejected, totalBlocked, stats ->
        HomeUiState(
            filterUnknownEnabled = filterEnabled,
            autoSmsEnabled = smsEnabled,
            todayRejectedCount = todayRejected,
            totalBlockedCount = totalBlocked,
            blockedStats = stats,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun setFilterUnknownEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFilterUnknownEnabled(enabled)
        }
    }

    fun setAutoSmsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSmsEnabled(enabled)
        }
    }
}
