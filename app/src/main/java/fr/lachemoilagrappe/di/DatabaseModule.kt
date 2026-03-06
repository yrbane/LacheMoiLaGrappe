package fr.lachemoilagrappe.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import fr.lachemoilagrappe.data.local.db.CallFilterDatabase
import fr.lachemoilagrappe.data.local.db.dao.CallLogDao
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
            .addMigrations(MIGRATION_1_2)
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
}
