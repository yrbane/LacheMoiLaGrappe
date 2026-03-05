package fr.lachemoilagrappe.data.repository

import fr.lachemoilagrappe.data.local.db.dao.UserListDao
import fr.lachemoilagrappe.data.local.db.entity.UserListEntry
import fr.lachemoilagrappe.domain.model.ListType
import fr.lachemoilagrappe.domain.repository.UserListRepository
import fr.lachemoilagrappe.util.PhoneNumberHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserListRepositoryImpl @Inject constructor(
    private val userListDao: UserListDao,
    private val phoneNumberHelper: PhoneNumberHelper
) : UserListRepository {

    override suspend fun isAllowed(number: String): Boolean {
        val normalized = phoneNumberHelper.normalize(number) ?: return false
        return userListDao.existsInList(normalized, ListType.ALLOW)
    }

    override suspend fun isBlocked(number: String): Boolean {
        val normalized = phoneNumberHelper.normalize(number) ?: return false
        return userListDao.existsInList(normalized, ListType.BLOCK)
    }

    override suspend fun getEntry(number: String): UserListEntry? {
        val normalized = phoneNumberHelper.normalize(number) ?: return null
        return userListDao.findByNumber(normalized)
    }

    override suspend fun addToAllowlist(number: String, label: String?) {
        val normalized = phoneNumberHelper.normalize(number) ?: return
        val entry = UserListEntry(
            normalizedNumber = normalized,
            listType = ListType.ALLOW,
            label = label,
            addedAt = System.currentTimeMillis()
        )
        userListDao.insert(entry)
    }

    override suspend fun addToBlocklist(number: String, label: String?) {
        val normalized = phoneNumberHelper.normalize(number) ?: return
        val entry = UserListEntry(
            normalizedNumber = normalized,
            listType = ListType.BLOCK,
            label = label,
            addedAt = System.currentTimeMillis()
        )
        userListDao.insert(entry)
    }

    override suspend fun remove(number: String) {
        val normalized = phoneNumberHelper.normalize(number) ?: return
        userListDao.deleteByNumber(normalized)
    }

    override fun getAllEntriesFlow(): Flow<List<UserListEntry>> {
        return userListDao.getAllFlow()
    }

    override fun getEntriesByTypeFlow(type: ListType): Flow<List<UserListEntry>> {
        return userListDao.getByTypeFlow(type)
    }
}
