package com.callfilter.di

import com.callfilter.data.local.preferences.SettingsRepositoryImpl
import com.callfilter.data.repository.CallLogRepositoryImpl
import com.callfilter.data.repository.ContactsRepositoryImpl
import com.callfilter.data.repository.SmsRepositoryImpl
import com.callfilter.data.repository.SpamRepositoryImpl
import com.callfilter.data.repository.UserListRepositoryImpl
import com.callfilter.domain.repository.CallLogRepository
import com.callfilter.domain.repository.ContactsRepository
import com.callfilter.domain.repository.SettingsRepository
import com.callfilter.domain.repository.SmsRepository
import com.callfilter.domain.repository.SpamRepository
import com.callfilter.domain.repository.UserListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContactsRepository(impl: ContactsRepositoryImpl): ContactsRepository

    @Binds
    @Singleton
    abstract fun bindSpamRepository(impl: SpamRepositoryImpl): SpamRepository

    @Binds
    @Singleton
    abstract fun bindUserListRepository(impl: UserListRepositoryImpl): UserListRepository

    @Binds
    @Singleton
    abstract fun bindCallLogRepository(impl: CallLogRepositoryImpl): CallLogRepository

    @Binds
    @Singleton
    abstract fun bindSmsRepository(impl: SmsRepositoryImpl): SmsRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
