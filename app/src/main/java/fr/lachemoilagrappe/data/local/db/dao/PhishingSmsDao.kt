package fr.lachemoilagrappe.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.lachemoilagrappe.data.local.db.entity.PhishingSmsEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface PhishingSmsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhishingSms(entry: PhishingSmsEntry): Long

    @Query("SELECT * FROM phishing_sms ORDER BY timestamp DESC")
    fun getAllPhishingSms(): Flow<List<PhishingSmsEntry>>

    @Query("DELETE FROM phishing_sms WHERE id = :id")
    suspend fun deletePhishingSms(id: Long)

    @Query("UPDATE phishing_sms SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("SELECT COUNT(*) FROM phishing_sms WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM phishing_sms WHERE timestamp >= :startOfDay")
    fun countBlockedToday(startOfDay: Long): Flow<Int>
}
