package com.callfilter.domain.repository

import com.callfilter.data.local.db.entity.SpamEntry
import kotlinx.coroutines.flow.Flow

data class SpamDbStats(
    val totalEntries: Int,
    val lastUpdateTime: Long?,
    val topTags: List<Pair<String, Int>>
)

interface SpamRepository {
    suspend fun lookupNumber(number: String): SpamEntry?
    suspend fun updateDatabase(entries: List<SpamEntry>)
    suspend fun getLastUpdateTime(): Long?
    suspend fun getStats(): SpamDbStats
    fun getEntryCountFlow(): Flow<Int>
    suspend fun clearDatabase()
}
