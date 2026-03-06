package fr.lachemoilagrappe.domain.repository

import fr.lachemoilagrappe.data.local.db.entity.PhishingSmsEntry
import fr.lachemoilagrappe.data.local.db.entity.SmsLogEntry
import fr.lachemoilagrappe.domain.model.SmsStatus
import kotlinx.coroutines.flow.Flow

interface SmsRepository {
    suspend fun canSendSms(number: String, cooldownHours: Int): Boolean
    suspend fun logSmsSent(number: String, normalizedNumber: String, template: String, status: SmsStatus): Long
    suspend fun updateSmsStatus(id: Long, status: SmsStatus)
    suspend fun getLastSmsSentTo(number: String): Long?
    fun getSmsHistoryFlow(): Flow<List<SmsLogEntry>>

    // Phishing SMS
    suspend fun logPhishingSms(phoneNumber: String, body: String, matchedKeyword: String?): Long
    fun getPhishingHistoryFlow(): Flow<List<PhishingSmsEntry>>
    suspend fun deletePhishingSms(id: Long)
    suspend fun markPhishingAsRead(id: Long)
    fun getUnreadPhishingCount(): Flow<Int>
    fun countBlockedPhishingToday(): Flow<Int>
}
