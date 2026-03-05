package fr.lachemoilagrappe.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.lachemoilagrappe.data.local.db.entity.CallLogEntry
import fr.lachemoilagrappe.domain.repository.CallLogRepository
import fr.lachemoilagrappe.domain.repository.UserListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val userListRepository: UserListRepository
) : ViewModel() {

    val callHistory: StateFlow<List<CallLogEntry>> = callLogRepository
        .getRecentCallsFlow(100)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun allowNumber(phoneNumber: String) {
        viewModelScope.launch {
            userListRepository.addToAllowlist(phoneNumber)
        }
    }

    fun blockNumber(phoneNumber: String) {
        viewModelScope.launch {
            userListRepository.addToBlocklist(phoneNumber)
        }
    }
}
