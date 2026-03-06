package fr.lachemoilagrappe.ui.screens.history

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.lachemoilagrappe.data.local.db.entity.CallLogEntry
import fr.lachemoilagrappe.data.local.db.entity.PhishingSmsEntry
import fr.lachemoilagrappe.domain.repository.CallLogRepository
import fr.lachemoilagrappe.domain.repository.SmsRepository
import fr.lachemoilagrappe.domain.repository.UserListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class HistoryTab {
    CALLS, SMS
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val smsRepository: SmsRepository,
    private val userListRepository: UserListRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    
    private val _selectedTab = MutableStateFlow(HistoryTab.CALLS)
    val selectedTab: StateFlow<HistoryTab> = _selectedTab

    @OptIn(ExperimentalCoroutinesApi::class)
    val callHistory: StateFlow<List<CallLogEntry>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                callLogRepository.getRecentCallsFlow(100)
            } else {
                callLogRepository.searchCallsFlow(query.trim(), 100)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val phishingSmsHistory: StateFlow<List<PhishingSmsEntry>> = smsRepository.getPhishingHistoryFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }
    
    fun selectTab(tab: HistoryTab) {
        _selectedTab.value = tab
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(300)
            _isRefreshing.value = false
        }
    }

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
    
    fun deletePhishingSms(id: Long) {
        viewModelScope.launch {
            smsRepository.deletePhishingSms(id)
        }
    }
    
    fun markPhishingAsRead(id: Long) {
        viewModelScope.launch {
            smsRepository.markPhishingAsRead(id)
        }
    }

    fun exportCsv(context: Context): Uri? {
        val entries = callHistory.value
        if (entries.isEmpty()) return null

        val dateFormatter = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
        val fileName = "lachemoilagrappe_export_${dateFormatter.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)

        val csvFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        file.bufferedWriter().use { writer ->
            writer.write("Date,Numéro,Contact,Décision,Raison")
            writer.newLine()
            entries.forEach { entry ->
                val date = csvFormatter.format(Date(entry.timestamp))
                val contact = entry.contactName?.replace(",", " ") ?: ""
                val number = entry.phoneNumber.replace(",", " ")
                val reason = entry.reason.replace(",", " ")
                writer.write("$date,$number,$contact,${entry.decision},$reason")
                writer.newLine()
            }
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
