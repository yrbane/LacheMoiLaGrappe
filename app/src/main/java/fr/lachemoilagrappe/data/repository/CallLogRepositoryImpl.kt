package fr.lachemoilagrappe.data.repository

import fr.lachemoilagrappe.data.local.db.dao.CallLogDao
import fr.lachemoilagrappe.data.local.db.dao.DayStat
import fr.lachemoilagrappe.data.local.db.entity.CallLogEntry
import fr.lachemoilagrappe.domain.model.CallDecision
import fr.lachemoilagrappe.domain.repository.CallLogRepository
import fr.lachemoilagrappe.util.PhoneNumberHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallLogRepositoryImpl @Inject constructor(
    private val callLogDao: CallLogDao,
    private val phoneNumberHelper: PhoneNumberHelper
) : CallLogRepository {

    override suspend fun logCall(entry: CallLogEntry): Long {
        return callLogDao.insert(entry)
    }

    override fun getAllCallsFlow(): Flow<List<CallLogEntry>> {
        return callLogDao.getAllCallsFlow()
    }

    override fun getRecentCallsFlow(limit: Int): Flow<List<CallLogEntry>> {
        return callLogDao.getRecentCallsFlow(limit)
    }

    override suspend fun getCallsByNumber(number: String): List<CallLogEntry> {
        val normalized = phoneNumberHelper.normalize(number) ?: return emptyList()
        return callLogDao.getCallsByNumber(normalized)
    }

    override fun getCallsByDecisionFlow(decision: CallDecision): Flow<List<CallLogEntry>> {
        return callLogDao.getCallsByDecisionFlow(decision)
    }

    override suspend fun getCallCountSince(since: Long): Int {
        return callLogDao.getCallCountSince(since)
    }

    override suspend fun getBlockedCountSince(since: Long): Int {
        return callLogDao.getBlockedCountSince(since)
    }

    override suspend fun getCallCountByNumberSince(normalizedNumber: String, since: Long): Int {
        return callLogDao.getCallCountByNumberSince(normalizedNumber, since)
    }

    override suspend fun deleteOlderThan(before: Long): Int {
        return callLogDao.deleteOlderThan(before)
    }

    override suspend fun getTotalBlockedCount(): Int {
        return callLogDao.getTotalBlockedCount()
    }

    override fun getBlockedCountSinceFlow(since: Long): Flow<Int> {
        return callLogDao.getBlockedCountSinceFlow(since)
    }

    override fun getTotalBlockedCountFlow(): Flow<Int> {
        return callLogDao.getTotalBlockedCountFlow()
    }

    override fun searchCallsFlow(query: String, limit: Int): Flow<List<CallLogEntry>> {
        return callLogDao.searchCallsFlow(query, limit)
    }

    override fun getBlockedStatsLastDays(days: Int): Flow<Map<Long, Int>> {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val startCal = Calendar.getInstance()
        startCal.timeInMillis = today.timeInMillis
        startCal.add(Calendar.DAY_OF_YEAR, -days + 1)
        val since = startCal.timeInMillis

        return callLogDao.getBlockedStatsSinceFlow(since).map { dayStats ->
            val statsMap = mutableMapOf<Long, Int>()
            
            // Initialize with 0 for each of the last X days in LOCAL timezone
            for (i in 0 until days) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = since
                cal.add(Calendar.DAY_OF_YEAR, i)
                // Normalize to midnight LOCAL
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                statsMap[cal.timeInMillis] = 0
            }

            // Fill with real data from DB
            dayStats.forEach { stat ->
                // Normalize the timestamp coming from SQL to match our LOCAL map keys
                val cal = Calendar.getInstance()
                cal.timeInMillis = stat.dayTimestamp
                // If SQL returned UTC midnight, but we are UTC+1, we shift it to match local day
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                
                val normalizedTimestamp = cal.timeInMillis
                if (statsMap.containsKey(normalizedTimestamp)) {
                    statsMap[normalizedTimestamp] = stat.count
                }
            }
            statsMap.toSortedMap()
        }
    }
}
