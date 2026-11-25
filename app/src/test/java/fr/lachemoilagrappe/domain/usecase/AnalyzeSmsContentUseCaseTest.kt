package fr.lachemoilagrappe.domain.usecase

import fr.lachemoilagrappe.domain.model.SmsAnalysisResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyzeSmsContentUseCaseTest {

    private val useCase = AnalyzeSmsContentUseCase()

    @Test
    fun `when SMS contains no keywords, result is not phishing`() {
        val result = useCase("Salut, tu viens ce soir ?")
        assertFalse(result.isPhishing)
        assertEquals(null, result.matchedKeyword)
    }

    @Test
    fun `when SMS contains CPF keyword, result is phishing`() {
        val result = useCase("Votre compte CPF arrive a expiration. Cliquez ici: http://bit.ly/cpf")
        assertTrue(result.isPhishing)
        assertEquals("cpf", result.matchedKeyword)
    }

    @Test
    fun `when SMS contains Amende keyword, result is phishing`() {
        val result = useCase("Info ANTAI: Vous avez une amende impayée. Payez sur www.amende-gouv.fr")
        assertTrue(result.isPhishing)
        assertEquals("amende", result.matchedKeyword)
    }

    @Test
    fun `when SMS contains Colis keyword and suspicious URL, result is phishing`() {
        val result = useCase("Chronopost: Votre colis est bloqué en douane. Réglez les frais ici: http://livraison.xyz")
        assertTrue(result.isPhishing)
        assertEquals("colis", result.matchedKeyword)
        assertTrue(result.hasSuspiciousLink)
    }

    @Test
    fun `when SMS contains normal conversation with colis and no link, result is NOT phishing`() {
        val result = useCase("Salut, tu as bien reçu mon colis hier ?")
        assertFalse("Normal conversation about colis without link or urgency should not be phishing", result.isPhishing)
    }

    @Test
    fun `when SMS contains high risk scam phrase without link, result is still phishing`() {
        val result = useCase("Info ANTAI: Vous avez une amende impayée. Payez sur www.amende-gouv.fr")
        assertTrue(result.isPhishing)
        assertEquals("amende", result.matchedKeyword)
        assertTrue(result.hasSuspiciousLink)
    }
    
    @Test
    fun `when SMS contains suspicious URL shortener, it flags hasSuspiciousLink`() {
        val result = useCase("Regarde ça : http://bit.ly/12345")
        assertFalse(result.isPhishing)
        assertTrue(result.hasSuspiciousLink)
    }
}
