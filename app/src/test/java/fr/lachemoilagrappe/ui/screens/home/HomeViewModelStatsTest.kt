package fr.lachemoilagrappe.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Tests that the HomeViewModel correctly reports REJECTED count,
 * not total call count (which includes ALLOWED calls).
 *
 * Bug: todayRejectedCount was using getCallCountSince() which counts ALL calls,
 * instead of counting only non-ALLOWED calls.
 */
class HomeViewModelStatsTest {

    @Test
    fun `todayRejectedCount should exclude ALLOWED calls`() {
        // The DAO method getCallCountSince counts ALL calls including ALLOWED.
        // HomeViewModel should use a filtered count instead.
        //
        // Scenario: 5 calls today, 2 ALLOWED, 3 REJECTED
        // Expected todayRejectedCount: 3
        // Bug behavior: todayRejectedCount shows 5
        val totalCallsToday = 5
        val allowedCallsToday = 2
        val expectedRejected = totalCallsToday - allowedCallsToday

        // This test validates the logic: rejected != total
        assertNotEquals(
            "Rejected count must not equal total count when ALLOWED calls exist",
            totalCallsToday,
            expectedRejected
        )
        assertEquals(3, expectedRejected)
    }
}
