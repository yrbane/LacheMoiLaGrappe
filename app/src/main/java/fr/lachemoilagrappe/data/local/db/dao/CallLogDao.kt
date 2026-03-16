package fr.lachemoilagrappe.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.lachemoilagrappe.data.local.db.entity.CallLogEntry
import fr.lachemoilagrappe.domain.model.CallDecision
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CallLogEntry): Long

    @Query("SELECT * FROM call_log ORDER BY timestamp DESC")
    fun getAllCallsFlow(): Flow<List<CallLogEntry>>

    @Query("SELECT * FROM call_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCallsFlow(limit: Int): Flow<List<CallLogEntry>>

    @Query("SELECT * FROM call_log WHERE normalizedNumber = :number ORDER BY timestamp DESC")
    suspend fun getCallsByNumber(number: String): List<CallLogEntry>

    @Query("SELECT * FROM call_log WHERE decision = :decision ORDER BY timestamp DESC")
    fun getCallsByDecisionFlow(decision: CallDecision): Flow<List<CallLogEntry>>

    @Query("SELECT COUNT(*) FROM call_log WHERE timestamp >= :since")
    suspend fun getCallCountSince(since: Long): Int

    @Query("SELECT COUNT(*) FROM call_log WHERE decision != 'ALLOWED' AND timestamp >= :since")
    suspend fun getBlockedCountSince(since: Long): Int

    @Query("SELECT COUNT(*) FROM call_log WHERE normalizedNumber = :normalizedNumber AND timestamp >= :since")
    suspend fun getCallCountByNumberSince(normalizedNumber: String, since: Long): Int

    @Query("SELECT COUNT(*) FROM call_log WHERE decision = :decision AND timestamp >= :since")
    suspend fun getCallCountByDecisionSince(decision: CallDecision, since: Long): Int

    @Query("DELETE FROM call_log WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM call_log WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long): Int

    @Query("DELETE FROM call_log")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM call_log")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM call_log WHERE decision != 'ALLOWED'")
    suspend fun getTotalBlockedCount(): Int

    @Query("SELECT COUNT(*) FROM call_log WHERE decision != 'ALLOWED' AND timestamp >= :since")
    fun getBlockedCountSinceFlow(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM call_log WHERE decision != 'ALLOWED'")
    fun getTotalBlockedCountFlow(): Flow<Int>

    @Query("SELECT * FROM call_log WHERE phoneNumber LIKE '%' || :query || '%' OR contactName LIKE '%' || :query || '%' OR normalizedNumber LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT :limit")
    fun searchCallsFlow(query: String, limit: Int = 100): Flow<List<CallLogEntry>>
}
