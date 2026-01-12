package com.callfilter.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.callfilter.data.local.db.entity.SpamEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SpamDao {

    @Query("SELECT * FROM spam_db WHERE normalizedNumber = :number LIMIT 1")
    suspend fun findByNumber(number: String): SpamEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<SpamEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SpamEntry)

    @Query("SELECT COUNT(*) FROM spam_db")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM spam_db")
    fun getCountFlow(): Flow<Int>

    @Query("SELECT tag, COUNT(*) as count FROM spam_db GROUP BY tag ORDER BY count DESC LIMIT :limit")
    suspend fun getTopTags(limit: Int): List<TagCount>

    @Query("SELECT MAX(updatedAt) FROM spam_db")
    suspend fun getLastUpdateTime(): Long?

    @Query("DELETE FROM spam_db")
    suspend fun deleteAll()

    @Query("DELETE FROM spam_db WHERE normalizedNumber = :number")
    suspend fun deleteByNumber(number: String)
}

data class TagCount(
    val tag: String,
    val count: Int
)
