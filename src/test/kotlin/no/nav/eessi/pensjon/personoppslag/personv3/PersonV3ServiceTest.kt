package no.nav.eessi.pensjon.personoppslag.personv3

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.*
import no.nav.eessi.pensjon.security.sts.STSClientConfig
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.xml.soap.SOAPFault
import javax.xml.ws.soap.SOAPFaultException

class PersonV3ServiceTest {

    private lateinit var personV3 : PersonV3
    private lateinit var stsClientConfig : STSClientConfig

    private lateinit var personV3Service : PersonV3Service

    private val subject = "23037329381"
    private val ikkeFunnetSubject = "33037329381"
    private val sikkerhetsbegrensingSubject = "43037329382"
    private val ugyldigIdSubject = "121212 4545"
    private val annenSoapIssueSubject = "annen soap issue"

    @BeforeEach
    fun setup() {
        stsClientConfig = mockk(relaxed = true)
        personV3 = mockk()

        val pv3s = PersonV3Service(personV3, stsClientConfig)
        pv3s.initMetrics()
        personV3Service = spyk(pv3s)

        every { personV3Service.konfigurerSamlToken() } just Runs

        every { personV3Service.hentPerson(subject) } returns PersonMock.createWith(subject)

        every { personV3Service.hentPerson(ikkeFunnetSubject) } returns null

        every { personV3Service.hentPerson(sikkerhetsbegrensingSubject) } throws
                PersonV3SikkerhetsbegrensningException("$sikkerhetsbegrensingSubject har sikkerhetsbegrensning")
    }

    @Test
    fun `Kaller hentPerson med gyldig subject`(){
        try {
            val person = personV3Service.hentPerson(subject)
            assertEquals("23037329381", (person!!.aktoer as PersonIdent).ident.ident)
        }catch(ex: Exception){
            assert(false)
        }
    }

    @Test
    fun `Kaller hentPerson med subject som ikke finnes`(){
        assertNull(personV3Service.hentPerson(ikkeFunnetSubject))
    }

    @Test
    fun `Kaller hentPerson med subject med sikkerhetsbegrensing`(){
        try {
            personV3Service.hentPerson(sikkerhetsbegrensingSubject)
            assert(false)
        }catch(ex: Exception){
            assert(ex is PersonV3SikkerhetsbegrensningException)
            assertEquals(ex.message, "$sikkerhetsbegrensingSubject har sikkerhetsbegrensning")
        }
    }

    @Test
    fun `Kaller hentPerson med ugyldig input (i følge TPS) - vi oppfører oss som om vi ikke finner svar`() {
        val soapFaultF002001F = mock<SOAPFault>()
        whenever(soapFaultF002001F.faultString).thenReturn("PersonV3: faultString: TPS svarte med FEIL, folgende status: F002001F og folgende melding: UGYLDIG VERDI I INPUT FNR")

        every { personV3.hentPerson(any()) } throws
                SOAPFaultException(soapFaultF002001F)

        assertNull(personV3Service.hentPerson(ugyldigIdSubject))
    }

    @Test
    fun `Kaller hentPerson med annen soap-feil`() {
        val soapFaultOther = mock<SOAPFault>()
        whenever(soapFaultOther.faultString).thenReturn("other")

        every { personV3.hentPerson(any()) } throws
                SOAPFaultException(soapFaultOther)

        assertThrows(SOAPFaultException::class.java) {
            personV3Service.hentPerson(annenSoapIssueSubject)
        }
    }

    @Test
    fun `hentPersonResponse med fodselsdato postfixa med 00000 gir UgyldigIdentException`() {
        val fakeNorskIdent = "230383" + "00000"
        val soapFaultOther = mock<SOAPFault>()
        whenever(soapFaultOther.faultString).thenReturn("TPS svarte med FEIL, folgende status: S610006F og folgende melding: FNR/DNR IKKE ENTYDIG")

        every { personV3.hentPerson(any()) } throws
                SOAPFaultException(soapFaultOther)

        assertThrows(UgyldigIdentException::class.java) {
            personV3Service.hentPersonResponse(fakeNorskIdent)
        }
    }

    @Test
    fun `hentPersonResponse ved SoapFaultException fra PersonV3 gir SoapFaultException`() {
        val soapFaultOther = mock<SOAPFault>()
        whenever(soapFaultOther.faultString).thenReturn("En eller annen fault")

        every { personV3.hentPerson(any()) } throws
                SOAPFaultException(soapFaultOther)

        assertThrows(SOAPFaultException::class.java) {
            personV3Service.hentPersonResponse("someident")
        }
    }

    @Test
    fun `hentPersonResponse ved annen Exception fra PersonV3 gir annen Exception`() {

        every { personV3.hentPerson(any()) } throws IOException()

        assertThrows(IOException::class.java) {
            personV3Service.hentPersonResponse("someident")
        }
    }

    @Test
    fun `Kaller hentBruker med fodselsdato postfixa med 00000`() {
        val fakeNorskIdent = "230383" + "00000"
        val soapFaultOther = mock<SOAPFault>()
        whenever(soapFaultOther.faultString).thenReturn("TPS svarte med FEIL, folgende status: S610006F og folgende melding: FNR/DNR IKKE ENTYDIG")

        every { personV3.hentPerson(any()) } throws
                SOAPFaultException(soapFaultOther)

        assertNull(personV3Service.hentBruker(fakeNorskIdent))
    }

    @Test
    fun `Maskerer fnr med * når fnr er 11 siffer og uten 00000, eller ignorerer denne om den har en avsluttende 00000`(){
        assert(personV3Service.maskerFnr("12345678912") == "123456*****")
        assert(personV3Service.maskerFnr("12345600000") == "12345600000")
    }
}
