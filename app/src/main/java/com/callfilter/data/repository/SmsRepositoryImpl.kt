package com.callfilter.data.repository

import com.callfilter.data.local.db.dao.SmsLogDao
import com.callfilter.data.local.db.entity.SmsLogEntry
import com.callfilter.domain.model.SmsStatus
import com.callfilter.domain.repository.SmsRepository
import com.callfilter.util.PhoneNumberHelper
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsRepositoryImpl @Inject constructor(
    private val smsLogDao: SmsLogDao,
    private val phoneNumberHelper: PhoneNumberHelper
) : SmsRepository {

    override suspend fun canSendSms(number: String, cooldownHours: Int): Boolean {
        val normalized = phoneNumberHelper.normalize(number) ?: return false
        val lastSent = smsLogDao.getLastSmsTimestamp(normalized, SmsStatus.SENT)

        if (lastSent == null) return true

        val cooldownMillis = TimeUnit.HOURS.toMillis(cooldownHours.toLong())
        val elapsed = System.currentTimeMillis() - lastSent

        return elapsed >= cooldownMillis
    }

    override suspend fun logSmsSent(
        number: String,
        normalizedNumber: String,
        template: String,
        status: SmsStatus
    ): Long {
        val entry = SmsLogEntry(
            phoneNumber = number,
            normalizedNumber = normalizedNumber,
            timestamp = System.currentTimeMillis(),
            status = status,
            templateUsed = template
        )
        return smsLogDao.insert(entry)
    }

    override suspend fun updateSmsStatus(id: Long, status: SmsStatus) {
        smsLogDao.updateStatus(id, status)
    }

    override suspend fun getLastSmsSentTo(number: String): Long? {
        val normalized = phoneNumberHelper.normalize(number) ?: return null
        return smsLogDao.getLastSmsTimestamp(normalized)
    }

    override fun getSmsHistoryFlow(): Flow<List<SmsLogEntry>> {
        return smsLogDao.getAllFlow()
    }
}
