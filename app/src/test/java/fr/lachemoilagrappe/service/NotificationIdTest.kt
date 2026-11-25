package fr.lachemoilagrappe.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationIdTest {

    @Test
    fun `computeNotificationId returns positive ID within bounds and applies offset`() {
        val phone = "+33612345678"
        val mainId = NotificationHelper.computeNotificationId(phone, NotificationHelper.OFFSET_MAIN)
        val confirmId = NotificationHelper.computeNotificationId(phone, NotificationHelper.OFFSET_SMS_CONFIRMATION)

        assertTrue(mainId >= 0)
        assertEquals(mainId + NotificationHelper.OFFSET_SMS_CONFIRMATION, confirmId)
    }
}
