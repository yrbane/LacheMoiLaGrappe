package fr.lachemoilagrappe.data.repository

import fr.lachemoilagrappe.data.local.db.dao.CallLogDao
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

    override suspend fun deleteOlderThan(before: Long): Int {
        return callLogDao.deleteOlderThan(before)
    }

    override suspend fun getTotalBlockedCount(): Int {
        return callLogDao.getTotalBlockedCount()
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

        return callLogDao.getAllCallsFlow().map { calls ->
            val stats = mutableMapOf<Long, Int>()
            val dayCal = Calendar.getInstance()
            
            // Initialize with 0
            for (i in 0 until days) {
                dayCal.timeInMillis = since
                dayCal.add(Calendar.DAY_OF_YEAR, i)
                stats[dayCal.timeInMillis] = 0
            }

            calls.filter { it.timestamp >= since && it.decision != CallDecision.ALLOWED }
                .forEach { call ->
                    dayCal.timeInMillis = call.timestamp
                    dayCal.set(Calendar.HOUR_OF_DAY, 0)
                    dayCal.set(Calendar.MINUTE, 0)
                    dayCal.set(Calendar.SECOND, 0)
                    dayCal.set(Calendar.MILLISECOND, 0)
                    val dayStart = dayCal.timeInMillis
                    if (stats.containsKey(dayStart)) {
                        stats[dayStart] = stats.getOrDefault(dayStart, 0) + 1
                    }
                }
            stats.toSortedMap()
        }
    }
}
