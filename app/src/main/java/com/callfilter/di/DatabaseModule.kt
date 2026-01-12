package com.callfilter.di

import android.content.Context
import androidx.room.Room
import com.callfilter.data.local.db.CallFilterDatabase
import com.callfilter.data.local.db.dao.CallLogDao
import com.callfilter.data.local.db.dao.SmsLogDao
import com.callfilter.data.local.db.dao.SpamDao
import com.callfilter.data.local.db.dao.UserListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CallFilterDatabase {
        return Room.databaseBuilder(
            context,
            CallFilterDatabase::class.java,
            CallFilterDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideCallLogDao(database: CallFilterDatabase): CallLogDao {
        return database.callLogDao()
    }

    @Provides
    fun provideSpamDao(database: CallFilterDatabase): SpamDao {
        return database.spamDao()
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
