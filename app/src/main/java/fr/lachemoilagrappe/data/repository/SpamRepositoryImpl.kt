package fr.lachemoilagrappe.data.repository

import fr.lachemoilagrappe.data.local.db.dao.SpamDao
import fr.lachemoilagrappe.data.local.db.entity.SpamEntry
import fr.lachemoilagrappe.domain.repository.SpamDbStats
import fr.lachemoilagrappe.domain.repository.SpamRepository
import fr.lachemoilagrappe.util.PhoneNumberHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpamRepositoryImpl @Inject constructor(
    private val spamDao: SpamDao,
    private val phoneNumberHelper: PhoneNumberHelper
) : SpamRepository {

    override suspend fun lookupNumber(number: String): SpamEntry? {
        val normalized = phoneNumberHelper.normalize(number) ?: return null
        return spamDao.findByNumber(normalized)
    }

    override suspend fun updateDatabase(entries: List<SpamEntry>) {
        spamDao.insertAll(entries)
    }

    override suspend fun getLastUpdateTime(): Long? {
        return spamDao.getLastUpdateTime()
    }

    override suspend fun getStats(): SpamDbStats {
        val totalEntries = spamDao.getCount()
        val lastUpdateTime = spamDao.getLastUpdateTime()
        val topTags = spamDao.getTopTags(5).map { it.tag to it.count }

        return SpamDbStats(
            totalEntries = totalEntries,
            lastUpdateTime = lastUpdateTime,
            topTags = topTags
        )
    }

    override fun getEntryCountFlow(): Flow<Int> {
        return spamDao.getCountFlow()
    }

    override suspend fun clearDatabase() {
        spamDao.deleteAll()
    }
}
