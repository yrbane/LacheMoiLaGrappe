package com.callfilter.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.callfilter.data.local.db.dao.CallLogDao
import com.callfilter.data.local.db.dao.SmsLogDao
import com.callfilter.data.local.db.dao.SpamDao
import com.callfilter.data.local.db.dao.UserListDao
import com.callfilter.data.local.db.entity.CallLogEntry
import com.callfilter.data.local.db.entity.SmsLogEntry
import com.callfilter.data.local.db.entity.SpamEntry
import com.callfilter.data.local.db.entity.UserListEntry

@Database(
    entities = [
        CallLogEntry::class,
        SpamEntry::class,
        UserListEntry::class,
        SmsLogEntry::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CallFilterDatabase : RoomDatabase() {

    abstract fun callLogDao(): CallLogDao
    abstract fun spamDao(): SpamDao
    abstract fun userListDao(): UserListDao
    abstract fun smsLogDao(): SmsLogDao

    companion object {
        const val DATABASE_NAME = "call_filter_db"
    }
}
