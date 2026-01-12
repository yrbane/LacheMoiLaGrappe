package com.callfilter.domain.repository

import com.callfilter.data.local.db.entity.UserListEntry
import com.callfilter.domain.model.ListType
import kotlinx.coroutines.flow.Flow

interface UserListRepository {
    suspend fun isAllowed(number: String): Boolean
    suspend fun isBlocked(number: String): Boolean
    suspend fun getEntry(number: String): UserListEntry?
    suspend fun addToAllowlist(number: String, label: String? = null)
    suspend fun addToBlocklist(number: String, label: String? = null)
    suspend fun remove(number: String)
    fun getAllEntriesFlow(): Flow<List<UserListEntry>>
    fun getEntriesByTypeFlow(type: ListType): Flow<List<UserListEntry>>
}
