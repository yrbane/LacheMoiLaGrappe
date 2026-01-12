package com.callfilter.domain.repository

import com.callfilter.data.local.db.entity.CallLogEntry
import com.callfilter.domain.model.CallDecision
import kotlinx.coroutines.flow.Flow

interface CallLogRepository {
    suspend fun logCall(entry: CallLogEntry): Long
    fun getAllCallsFlow(): Flow<List<CallLogEntry>>
    fun getRecentCallsFlow(limit: Int = 50): Flow<List<CallLogEntry>>
    suspend fun getCallsByNumber(number: String): List<CallLogEntry>
    fun getCallsByDecisionFlow(decision: CallDecision): Flow<List<CallLogEntry>>
    suspend fun getCallCountSince(since: Long): Int
    suspend fun deleteOlderThan(before: Long): Int
}
