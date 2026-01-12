package com.callfilter.data.repository

import com.callfilter.data.local.db.dao.CallLogDao
import com.callfilter.data.local.db.entity.CallLogEntry
import com.callfilter.domain.model.CallDecision
import com.callfilter.domain.repository.CallLogRepository
import com.callfilter.util.PhoneNumberHelper
import kotlinx.coroutines.flow.Flow
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
}
