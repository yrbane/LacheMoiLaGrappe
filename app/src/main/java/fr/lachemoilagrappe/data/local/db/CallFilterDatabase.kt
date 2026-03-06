package fr.lachemoilagrappe.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.lachemoilagrappe.data.local.db.dao.CallLogDao
import fr.lachemoilagrappe.data.local.db.dao.PhishingSmsDao
import fr.lachemoilagrappe.data.local.db.dao.SmsLogDao
import fr.lachemoilagrappe.data.local.db.dao.UserListDao
import fr.lachemoilagrappe.data.local.db.entity.CallLogEntry
import fr.lachemoilagrappe.data.local.db.entity.PhishingSmsEntry
import fr.lachemoilagrappe.data.local.db.entity.SmsLogEntry
import fr.lachemoilagrappe.data.local.db.entity.UserListEntry

@Database(
    entities = [
        CallLogEntry::class,
        UserListEntry::class,
        SmsLogEntry::class,
        PhishingSmsEntry::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CallFilterDatabase : RoomDatabase() {

    abstract fun callLogDao(): CallLogDao
    abstract fun userListDao(): UserListDao
    abstract fun smsLogDao(): SmsLogDao
    abstract fun phishingSmsDao(): PhishingSmsDao

    companion object {
        const val DATABASE_NAME = "call_filter_db"
    }
}
