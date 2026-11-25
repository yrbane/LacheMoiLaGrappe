package fr.lachemoilagrappe.ui.screens.home

import fr.lachemoilagrappe.domain.repository.CallLogRepository
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelStatsTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var callLogRepository: CallLogRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mockk()
        callLogRepository = mockk()

        every { settingsRepository.filterUnknownEnabled } returns flowOf(true)
        every { settingsRepository.autoSmsEnabled } returns flowOf(false)
        every { callLogRepository.getBlockedCountSinceFlow(any()) } returns flowOf(3)
        every { callLogRepository.getTotalBlockedCountFlow() } returns flowOf(42)
        every { callLogRepository.getBlockedStatsLastDays(7) } returns flowOf(mapOf(1000L to 3))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `HomeViewModel emits todayRejectedCount and totalBlockedCount from repository flows`() = runTest {
        val viewModel = HomeViewModel(settingsRepository, callLogRepository)

        val state = viewModel.uiState.first { !it.isLoading }

        assertFalse(state.isLoading)
        assertEquals(3, state.todayRejectedCount)
        assertEquals(42, state.totalBlockedCount)
        assertEquals(true, state.filterUnknownEnabled)
        assertEquals(false, state.autoSmsEnabled)
        assertEquals(mapOf(1000L to 3), state.blockedStats)
    }
}
