package no.nav.eessi.pensjon.personoppslag.aktoerregister

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(MockitoExtension::class)
class AktoerregisterServiceTest {

    @Mock
    private lateinit var mockrestTemplate: RestTemplate

    lateinit var aktoerregisterService: AktoerregisterService

    @BeforeEach
    fun setup() {
        aktoerregisterService = AktoerregisterService(mockrestTemplate, appName = "unittests")
        aktoerregisterService.initMetrics()
    }


    @Test
    fun `hentGjeldendeNorskIdentForAktorId() should return 1 NorskIdent`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/aktoerregister/200-OK_1-IdentinfoForAktoer-with-1-gjeldende-AktoerId.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "1000101917358"
        val expectedNorskIdent = "18128126178"

        val response = aktoerregisterService.hentGjeldendeNorskIdentForAktorId(testAktoerId)
        assertEquals(expectedNorskIdent, response, "AktørId 1000101917358 har norskidenten 18128126178")
    }


    @Test
    fun `hentGjeldendeNorskIdentForAktorId() should return 1 AktoerId`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/aktoerregister/200-OK_1-IdentinfoForAktoer-with-1-gjeldende-NorskIdent.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))).thenReturn(mockResponseEntity)

        val inputNorskIdent = "18128126178"
        val expectedAktoerId = "1000101917358"

        val response = aktoerregisterService.hentGjeldendeAktorIdForNorskIdent(inputNorskIdent)
        assertEquals(expectedAktoerId, response,"NorskIdent 18128126178 skal ha AktoerId 100010191735818128126178")
    }


    @Test
    fun `hentGjeldendeNorskIdentForAktorId() should fail if ident is not found in response`() {
        // the mock returns NorskIdent 18128126178, not 1234 as we ask for
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/aktoerregister/200-OK_1-IdentinfoForAktoer-with-1-gjeldende-AktoerId.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "1234"

        val rte = assertThrows<AktoerregisterIkkeFunnetException> {
            aktoerregisterService.hentGjeldendeNorskIdentForAktorId(testAktoerId)
        }
        assertTrue(rte.message!!.contains(testAktoerId))
    }

    @Test
    fun `should throw runtimeexception if no ident is found in response`() {
        // the mock returns a valid response, but has no idents
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/aktoerregister/200-OK_0-IdentinfoForAktoer.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "18128126178"

        val rte = assertThrows<AktoerregisterIkkeFunnetException> {
            aktoerregisterService.hentGjeldendeNorskIdentForAktorId(testAktoerId)
        }
        assertTrue(rte.message!!.contains(testAktoerId), "Exception skal si noe om hvilken identen som ikke ble funnet")
    }


    @Test
    fun `AktoerregisterException should be thrown when response contains a 'feilmelding'`() {
        // the mock returns a valid response, but with a message in 'feilmelding'
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/aktoerregister/200-OK_1-IdentinfoForAktoer-with-errormsg.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "10000609641830456"

        val are = assertThrows<AktoerregisterException> {
            aktoerregisterService.hentGjeldendeNorskIdentForAktorId(testAktoerId)
        }
        assertEquals("Den angitte personidenten finnes ikke", are.message!!, "Feilmeldingen fra aktørregisteret skal være exception-message")
    }

    @Test
    fun `should throw runtimeexception when multiple idents are returned`() {
        // the mock returns a valid response, but has 2 gjeldende AktoerId
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/aktoerregister/200-OK_1-IdentinfoForAktoer-with-2-gjeldende-AktoerId.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "1000101917358"

        val rte = assertThrows<AktoerregisterException> {
            aktoerregisterService.hentGjeldendeAktorIdForNorskIdent(testAktoerId)
        }
        assertEquals("Forventet 1 gjeldende AktoerId, fant 2", rte.message!!)
    }

    @Test
    fun `should throw runtimeexception when 403-forbidden is returned`() {
        // the mock returns 403-forbidden
        doThrow(HttpClientErrorException(HttpStatus.FORBIDDEN))
                .whenever(mockrestTemplate).exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))

        val testAktoerId = "does-not-matter"

        val rte = assertThrows<AktoerregisterException> {
            aktoerregisterService.hentGjeldendeAktorIdForNorskIdent(testAktoerId)
        }
        assertTrue(rte.message!!.contains("403 FORBIDDEN"))
    }

    @Test
    fun `hentGjeldendeIdent() should return 1 NorskIdent`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/aktoerregister/200-OK_1-IdentinfoForAktoer-with-1-gjeldende-AktoerId.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))).thenReturn(mockResponseEntity)

        val input = AktoerId("1000101917358")
        val expected = NorskIdent("18128126178")

        val result = aktoerregisterService.hentGjeldendeIdent(IdentGruppe.NorskIdent, input)

        assertEquals(expected, result)
    }


    @Test
    fun `hentGjeldendeIdent() should return 1 AktoerId`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/aktoerregister/200-OK_1-IdentinfoForAktoer-with-1-gjeldende-NorskIdent.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))).thenReturn(mockResponseEntity)

        val input = NorskIdent("18128126178")
        val expected = AktoerId("1000101917358")

        val result = aktoerregisterService.hentGjeldendeIdent(IdentGruppe.AktoerId, input)

        assertEquals(expected, result)
    }


    @Test
    fun `hentGjeldendeIdent() should return null if ident is not found in response`() {
        // the mock returns NorskIdent 18128126178, not 1234 as we ask for
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/aktoerregister/200-OK_1-IdentinfoForAktoer-with-1-gjeldende-AktoerId.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))).thenReturn(mockResponseEntity)

        val input = AktoerId("1234")

        val result = aktoerregisterService.hentGjeldendeIdent(IdentGruppe.NorskIdent, input)

        assertNull(result)
    }

    @Test
    fun `hentGjeldendeIdent() should return null if no ident is found in response`() {
        // the mock returns a valid response, but has no idents
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/aktoerregister/200-OK_0-IdentinfoForAktoer.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))).thenReturn(mockResponseEntity)

        val input = AktoerId("18128126178")

        val result = aktoerregisterService.hentGjeldendeIdent(IdentGruppe.NorskIdent, input)

        assertNull(result)
    }


    @Test
    fun `hentGjeldendeIdent() should throw exception when response contains a 'feilmelding'`() {
        // the mock returns a valid response, but with a message in 'feilmelding'
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/aktoerregister/200-OK_1-IdentinfoForAktoer-with-errormsg.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))).thenReturn(mockResponseEntity)

        val input = AktoerId("10000609641830456")

        val exception = assertThrows<AktoerregisterException> {
            aktoerregisterService.hentGjeldendeIdent(IdentGruppe.NorskIdent, input)
        }
        assertEquals("Den angitte personidenten finnes ikke", exception.message!!, "Feilmeldingen fra aktørregisteret skal være exception-message")
   }

    @Test
    fun `hentGjeldendeIdent() should throw exception when multiple idents are returned`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/aktoerregister/200-OK_1-IdentinfoForAktoer-with-2-gjeldende-AktoerId.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))).thenReturn(mockResponseEntity)

        val input = NorskIdent("1000101917358")

        val exception = assertThrows<AktoerregisterException> {
            aktoerregisterService.hentGjeldendeIdent(IdentGruppe.AktoerId, input)
        }

        assertEquals("Forventet 1 gjeldende AktoerId, fant 2", exception.message!!)
    }

    @Test
    fun `hentGjeldendeIdent() should throw exception when 403-forbidden is returned`() {
        doThrow(HttpClientErrorException(HttpStatus.FORBIDDEN))
                .whenever(mockrestTemplate).exchange(any<String>(), any(), any<HttpEntity<Unit>>(), eq(String::class.java))

        val input = NorskIdent("does-not-matter")

        val exception = assertThrows<AktoerregisterException> {
            aktoerregisterService.hentGjeldendeIdent(IdentGruppe.AktoerId, input)
        }

        assertTrue(exception.message!!.contains("403 FORBIDDEN"))
    }

    private fun createResponseEntityFromJsonFile(filePath: String, httpStatus: HttpStatus = HttpStatus.OK): ResponseEntity<String> {
        val mockResponseString = String(Files.readAllBytes(Paths.get(filePath)))
        return ResponseEntity(mockResponseString, httpStatus)
    }
}
