package com.callfilter.domain.repository

import com.callfilter.data.local.db.entity.SmsLogEntry
import com.callfilter.domain.model.SmsStatus
import kotlinx.coroutines.flow.Flow

interface SmsRepository {
    suspend fun canSendSms(number: String, cooldownHours: Int): Boolean
    suspend fun logSmsSent(number: String, normalizedNumber: String, template: String, status: SmsStatus): Long
    suspend fun updateSmsStatus(id: Long, status: SmsStatus)
    suspend fun getLastSmsSentTo(number: String): Long?
    fun getSmsHistoryFlow(): Flow<List<SmsLogEntry>>
}
