package com.callfilter.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.callfilter.data.local.db.entity.UserListEntry
import com.callfilter.domain.model.ListType
import kotlinx.coroutines.flow.Flow

@Dao
interface UserListDao {

    @Query("SELECT * FROM user_list WHERE normalizedNumber = :number LIMIT 1")
    suspend fun findByNumber(number: String): UserListEntry?

    @Query("SELECT * FROM user_list WHERE listType = :type ORDER BY addedAt DESC")
    fun getByTypeFlow(type: ListType): Flow<List<UserListEntry>>

    @Query("SELECT * FROM user_list ORDER BY addedAt DESC")
    fun getAllFlow(): Flow<List<UserListEntry>>

    @Query("SELECT EXISTS(SELECT 1 FROM user_list WHERE normalizedNumber = :number AND listType = :type)")
    suspend fun existsInList(number: String, type: ListType): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: UserListEntry)

    @Query("DELETE FROM user_list WHERE normalizedNumber = :number")
    suspend fun deleteByNumber(number: String)

    @Query("DELETE FROM user_list WHERE listType = :type")
    suspend fun deleteAllByType(type: ListType)

    @Query("DELETE FROM user_list")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM user_list WHERE listType = :type")
    suspend fun getCountByType(type: ListType): Int
}
