package fr.lachemoilagrappe.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import fr.lachemoilagrappe.data.local.db.CallFilterDatabase
import fr.lachemoilagrappe.data.local.db.dao.CallLogDao
import fr.lachemoilagrappe.data.local.db.dao.PhishingSmsDao
import fr.lachemoilagrappe.data.local.db.dao.SmsLogDao
import fr.lachemoilagrappe.data.local.db.dao.UserListDao
import fr.lachemoilagrappe.util.DatabaseKeyHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS spam_db")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS phishing_sms (
                id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
                phoneNumber TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                body TEXT NOT NULL,
                matchedKeyword TEXT,
                isRead INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_phishing_sms_phoneNumber ON phishing_sms(phoneNumber)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_phishing_sms_timestamp ON phishing_sms(timestamp)")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CallFilterDatabase {
        val passphrase = DatabaseKeyHelper.getPassphrase(context)
        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            CallFilterDatabase::class.java,
            CallFilterDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideCallLogDao(database: CallFilterDatabase): CallLogDao {
        return database.callLogDao()
    }

    @Provides
    fun provideUserListDao(database: CallFilterDatabase): UserListDao {
        return database.userListDao()
    }

    @Provides
    fun provideSmsLogDao(database: CallFilterDatabase): SmsLogDao {
        return database.smsLogDao()
    }

    @Provides
    fun providePhishingSmsDao(database: CallFilterDatabase): PhishingSmsDao {
        return database.phishingSmsDao()
    }
}
