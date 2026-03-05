package fr.lachemoilagrappe.di

import fr.lachemoilagrappe.data.local.preferences.SettingsRepositoryImpl
import fr.lachemoilagrappe.data.repository.CallLogRepositoryImpl
import fr.lachemoilagrappe.data.repository.ContactsRepositoryImpl
import fr.lachemoilagrappe.data.repository.SmsRepositoryImpl
import fr.lachemoilagrappe.data.repository.SpamRepositoryImpl
import fr.lachemoilagrappe.data.repository.UserListRepositoryImpl
import fr.lachemoilagrappe.domain.repository.CallLogRepository
import fr.lachemoilagrappe.domain.repository.ContactsRepository
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import fr.lachemoilagrappe.domain.repository.SmsRepository
import fr.lachemoilagrappe.domain.repository.SpamRepository
import fr.lachemoilagrappe.domain.repository.UserListRepository
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
