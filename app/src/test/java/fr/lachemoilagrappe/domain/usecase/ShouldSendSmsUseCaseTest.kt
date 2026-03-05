package fr.lachemoilagrappe.domain.usecase

import fr.lachemoilagrappe.domain.model.SmsDecision
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import fr.lachemoilagrappe.domain.repository.SmsRepository
import fr.lachemoilagrappe.util.PhoneNumberHelper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShouldSendSmsUseCaseTest {

    private lateinit var smsRepository: SmsRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var phoneNumberHelper: PhoneNumberHelper
    private lateinit var useCase: ShouldSendSmsUseCase

    @Before
    fun setup() {
        smsRepository = mockk()
        settingsRepository = mockk()
        phoneNumberHelper = mockk()

        // Default behavior: SMS enabled, mobile number, cooldown OK, no confirmation
        coEvery { settingsRepository.getAutoSmsEnabled() } returns true
        coEvery { settingsRepository.getSmsCooldownHours() } returns 24
        coEvery { settingsRepository.getSmsConfirmationMode() } returns false
        coEvery { smsRepository.canSendSms(any(), any()) } returns true
        every { phoneNumberHelper.shouldExcludeFromSms(any()) } returns false
        every { phoneNumberHelper.isMobileNumber(any()) } returns true

        useCase = ShouldSendSmsUseCase(
            smsRepository,
            settingsRepository,
            phoneNumberHelper
        )
    }

    // === AUTO SMS DISABLED ===

    @Test
    fun `returns Skip when auto SMS is disabled`() = runTest {
        coEvery { settingsRepository.getAutoSmsEnabled() } returns false

        val result = useCase("+33612345678")

        assertTrue(result is SmsDecision.Skip)
        assertEquals("SMS automatique désactivé", (result as SmsDecision.Skip).reason)
    }

    // === EXCLUDED NUMBERS ===

    @Test
    fun `returns Skip for emergency numbers`() = runTest {
        every { phoneNumberHelper.shouldExcludeFromSms("15") } returns true

        val result = useCase("15")

        assertTrue(result is SmsDecision.Skip)
        assertEquals("Numéro exclu (urgence, court ou masqué)", (result as SmsDecision.Skip).reason)
    }

    @Test
    fun `returns Skip for short numbers`() = runTest {
        every { phoneNumberHelper.shouldExcludeFromSms("3615") } returns true

        val result = useCase("3615")

        assertTrue(result is SmsDecision.Skip)
    }

    @Test
    fun `returns Skip for hidden numbers`() = runTest {
        every { phoneNumberHelper.shouldExcludeFromSms("") } returns true

        val result = useCase("")

        assertTrue(result is SmsDecision.Skip)
    }

    // === NON-MOBILE NUMBERS ===

    @Test
    fun `returns Skip for landline numbers`() = runTest {
        every { phoneNumberHelper.isMobileNumber("0145678900") } returns false

        val result = useCase("0145678900")

        assertTrue(result is SmsDecision.Skip)
        assertEquals("Numéro non mobile", (result as SmsDecision.Skip).reason)
    }

    @Test
    fun `returns Send for mobile numbers`() = runTest {
        every { phoneNumberHelper.isMobileNumber("+33612345678") } returns true

        val result = useCase("+33612345678")

        assertEquals(SmsDecision.Send, result)
    }

    // === COOLDOWN ===

    @Test
    fun `returns Skip when cooldown not elapsed`() = runTest {
        coEvery { smsRepository.canSendSms("+33612345678", 24) } returns false

        val result = useCase("+33612345678")

        assertTrue(result is SmsDecision.Skip)
        assertEquals("Cooldown non écoulé", (result as SmsDecision.Skip).reason)
    }

    @Test
    fun `returns Send when cooldown elapsed`() = runTest {
        coEvery { smsRepository.canSendSms("+33612345678", 24) } returns true

        val result = useCase("+33612345678")

        assertEquals(SmsDecision.Send, result)
    }

    @Test
    fun `uses configured cooldown hours`() = runTest {
        coEvery { settingsRepository.getSmsCooldownHours() } returns 48
        coEvery { smsRepository.canSendSms("+33612345678", 48) } returns true

        val result = useCase("+33612345678")

        assertEquals(SmsDecision.Send, result)
    }

    // === CONFIRMATION MODE ===

    @Test
    fun `returns AskConfirmation when confirmation mode is enabled`() = runTest {
        coEvery { settingsRepository.getSmsConfirmationMode() } returns true

        val result = useCase("+33612345678")

        assertEquals(SmsDecision.AskConfirmation, result)
    }

    @Test
    fun `returns Send when confirmation mode is disabled`() = runTest {
        coEvery { settingsRepository.getSmsConfirmationMode() } returns false

        val result = useCase("+33612345678")

        assertEquals(SmsDecision.Send, result)
    }

    // === COMPLETE FLOW ===

    @Test
    fun `full happy path returns Send`() = runTest {
        // All conditions met
        coEvery { settingsRepository.getAutoSmsEnabled() } returns true
        every { phoneNumberHelper.shouldExcludeFromSms("+33612345678") } returns false
        every { phoneNumberHelper.isMobileNumber("+33612345678") } returns true
        coEvery { settingsRepository.getSmsCooldownHours() } returns 24
        coEvery { smsRepository.canSendSms("+33612345678", 24) } returns true
        coEvery { settingsRepository.getSmsConfirmationMode() } returns false

        val result = useCase("+33612345678")

        assertEquals(SmsDecision.Send, result)
    }

    @Test
    fun `checks conditions in correct order`() = runTest {
        // Auto SMS disabled should return early without checking other conditions
        coEvery { settingsRepository.getAutoSmsEnabled() } returns false
        // These should not be called
        every { phoneNumberHelper.shouldExcludeFromSms(any()) } throws AssertionError("Should not be called")
        every { phoneNumberHelper.isMobileNumber(any()) } throws AssertionError("Should not be called")

        val result = useCase("+33612345678")

        assertTrue(result is SmsDecision.Skip)
    }
}
