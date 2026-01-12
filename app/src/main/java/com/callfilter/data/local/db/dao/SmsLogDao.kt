package com.callfilter.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.callfilter.data.local.db.entity.SmsLogEntry
import com.callfilter.domain.model.SmsStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SmsLogEntry): Long

    @Query("SELECT * FROM sms_log ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<SmsLogEntry>>

    @Query("SELECT * FROM sms_log WHERE normalizedNumber = :number ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSmsToNumber(number: String): SmsLogEntry?

    @Query("SELECT MAX(timestamp) FROM sms_log WHERE normalizedNumber = :number AND status = :status")
    suspend fun getLastSmsTimestamp(number: String, status: SmsStatus = SmsStatus.SENT): Long?

    @Query("SELECT COUNT(*) FROM sms_log WHERE timestamp >= :since")
    suspend fun getCountSince(since: Long): Int

    @Query("UPDATE sms_log SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: SmsStatus)

    @Query("DELETE FROM sms_log WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long): Int

    @Query("DELETE FROM sms_log")
    suspend fun deleteAll()
}
