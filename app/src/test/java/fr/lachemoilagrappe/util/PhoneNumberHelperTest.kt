package fr.lachemoilagrappe.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PhoneNumberHelperTest {

    private lateinit var helper: PhoneNumberHelper

    @Before
    fun setup() {
        helper = PhoneNumberHelper()
    }

    @Test
    fun `normalize formats French number to E164`() {
        assertEquals("+33612345678", helper.normalize("06 12 34 56 78"))
        assertEquals("+33145678900", helper.normalize("01.45.67.89.00"))
    }

    @Test
    fun `isFixedLine returns true for French landlines 01-05 and false for mobiles 06-07`() {
        assertTrue(helper.isFixedLine("0145678900"))
        assertTrue(helper.isFixedLine("+33473123456"))
        assertFalse(helper.isFixedLine("0612345678"))
        assertFalse(helper.isFixedLine("+33712345678"))
        assertFalse(helper.isFixedLine("0912345678"))
    }

    @Test
    fun `isMobileNumber returns true for 06 and 07 and false for 01`() {
        assertTrue(helper.isMobileNumber("0612345678"))
        assertTrue(helper.isMobileNumber("+33712345678"))
        assertFalse(helper.isMobileNumber("0145678900"))
    }
}
