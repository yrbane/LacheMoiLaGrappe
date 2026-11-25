package fr.lachemoilagrappe.ui.screens.history

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvSanitizerTest {

    @Test
    fun `sanitizeCsvField escapes formula prefixes and quotes properly`() {
        assertEquals("\"'=CMD|' /C calc'!A0\"", HistoryViewModel.sanitizeCsvField("=CMD|' /C calc'!A0"))
        assertEquals("\"'+33612345678\"", HistoryViewModel.sanitizeCsvField("+33612345678"))
        assertEquals("\"-12345\"", HistoryViewModel.sanitizeCsvField("-12345"))
        assertEquals("\"'@admin\"", HistoryViewModel.sanitizeCsvField("@admin"))
        assertEquals("\"Dupont, Jean\"", HistoryViewModel.sanitizeCsvField("Dupont, Jean"))
        assertEquals("\"Ligne 1 Ligne 2\"", HistoryViewModel.sanitizeCsvField("Ligne 1\nLigne 2"))
        assertEquals("\"Test \"\"Quote\"\"\"", HistoryViewModel.sanitizeCsvField("Test \"Quote\""))
    }
}
