package fr.lachemoilagrappe.data.repository

import fr.lachemoilagrappe.data.local.db.dao.PhishingSmsDao
import fr.lachemoilagrappe.data.local.db.dao.SmsLogDao
import fr.lachemoilagrappe.data.local.db.entity.PhishingSmsEntry
import fr.lachemoilagrappe.data.local.db.entity.SmsLogEntry
import fr.lachemoilagrappe.domain.model.SmsStatus
import fr.lachemoilagrappe.domain.repository.SmsRepository
import fr.lachemoilagrappe.util.PhoneNumberHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsRepositoryImpl @Inject constructor(
    private val smsLogDao: SmsLogDao,
    private val phishingSmsDao: PhishingSmsDao,
    private val phoneNumberHelper: PhoneNumberHelper
) : SmsRepository {

    // Per-number mutex to prevent race conditions on cooldown checks
    private val numberLocks = ConcurrentHashMap<String, Mutex>()

    private fun getMutexForNumber(normalizedNumber: String): Mutex {
        return numberLocks.getOrPut(normalizedNumber) { Mutex() }
    }

    override suspend fun canSendSms(number: String, cooldownHours: Int): Boolean {
        val normalized = phoneNumberHelper.normalize(number) ?: return false

        // Use per-number lock to prevent race conditions
        return getMutexForNumber(normalized).withLock {
            val lastSent = smsLogDao.getLastSmsTimestamp(normalized, SmsStatus.SENT)

            if (lastSent == null) return@withLock true

            val cooldownMillis = TimeUnit.HOURS.toMillis(cooldownHours.toLong())
            val elapsed = System.currentTimeMillis() - lastSent

            elapsed >= cooldownMillis
        }
    }

    override suspend fun logSmsSent(
        number: String,
        normalizedNumber: String,
        template: String,
        status: SmsStatus
    ): Long {
        // Use same lock to ensure atomicity with canSendSms check
        return getMutexForNumber(normalizedNumber).withLock {
            val entry = SmsLogEntry(
                phoneNumber = number,
                normalizedNumber = normalizedNumber,
                timestamp = System.currentTimeMillis(),
                status = status,
                templateUsed = template
            )
            smsLogDao.insert(entry)
        }
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

    override suspend fun logPhishingSms(
        phoneNumber: String,
        body: String,
        matchedKeyword: String?
    ): Long {
        val entry = PhishingSmsEntry(
            phoneNumber = phoneNumber,
            timestamp = System.currentTimeMillis(),
            body = body,
            matchedKeyword = matchedKeyword
        )
        return phishingSmsDao.insertPhishingSms(entry)
    }

    override fun getPhishingHistoryFlow(): Flow<List<PhishingSmsEntry>> {
        return phishingSmsDao.getAllPhishingSms()
    }

    override suspend fun deletePhishingSms(id: Long) {
        phishingSmsDao.deletePhishingSms(id)
    }

    override suspend fun markPhishingAsRead(id: Long) {
        phishingSmsDao.markAsRead(id)
    }

    override fun getUnreadPhishingCount(): Flow<Int> {
        return phishingSmsDao.getUnreadCount()
    }

    override fun countBlockedPhishingToday(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return phishingSmsDao.countBlockedToday(calendar.timeInMillis)
    }
}
