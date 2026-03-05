package fr.lachemoilagrappe.ui.screens.userlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lachemoilagrappe.data.local.db.entity.UserListEntry
import fr.lachemoilagrappe.domain.model.ListType
import fr.lachemoilagrappe.domain.repository.UserListRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserListsViewModel @Inject constructor(
    private val userListRepository: UserListRepository
) : ViewModel() {

    val allowlist: StateFlow<List<UserListEntry>> = userListRepository
        .getEntriesByTypeFlow(ListType.ALLOW)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val blocklist: StateFlow<List<UserListEntry>> = userListRepository
        .getEntriesByTypeFlow(ListType.BLOCK)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addToAllowlist(number: String, label: String? = null) {
        viewModelScope.launch {
            userListRepository.addToAllowlist(number, label)
        }
    }

    fun addToBlocklist(number: String, label: String? = null) {
        viewModelScope.launch {
            userListRepository.addToBlocklist(number, label)
        }
    }

    fun removeEntry(entry: UserListEntry) {
        viewModelScope.launch {
            userListRepository.remove(entry.normalizedNumber)
        }
    }
}
