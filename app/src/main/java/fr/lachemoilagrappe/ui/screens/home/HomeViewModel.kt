package fr.lachemoilagrappe.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.lachemoilagrappe.domain.repository.CallLogRepository
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _todayRejectedCount = MutableStateFlow(0)
    private val _totalBlockedCount = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.filterUnknownEnabled,
        settingsRepository.autoSmsEnabled,
        combine(_todayRejectedCount, _totalBlockedCount) { r, t -> Pair(r, t) },
        callLogRepository.getBlockedStatsLastDays(7)
    ) { filterEnabled, smsEnabled, (rejected, total), stats ->
        HomeUiState(
            filterUnknownEnabled = filterEnabled,
            autoSmsEnabled = smsEnabled,
            todayRejectedCount = rejected,
            totalBlockedCount = total,
            blockedStats = stats,
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
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            _todayRejectedCount.value = callLogRepository.getCallCountSince(todayStart)
            _totalBlockedCount.value = callLogRepository.getTotalBlockedCount()
        }
    }

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
