package no.nav.eessi.pensjon.personoppslag.pdl

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseGradering
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentGruppe
import no.nav.eessi.pensjon.personoppslag.pdl.model.NorskIdent
import no.nav.eessi.pensjon.personoppslag.pdl.model.SokKriterier
import no.nav.eessi.pensjon.security.sts.STSService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import java.time.LocalDate

@Disabled
internal class PersonServiceIntegrationTest {

    /**
     * Paste valid token
     */
    private val mockStsService = mockk<STSService> {
        every {
            getSystemOidcToken()
        } returns ""
    }

    private val mockClient = PdlConfigurationImp(mockStsService)
            .pdlRestTemplate(RestTemplateBuilder())

    /**
     * Use local port forwarding using kubectl and nais
     *
     * Example: kubectl port-forward svc/pdl-api 8089:80
     */
    private val service = PersonService(
            PersonClient(mockClient, "https://pdl-api.dev.intern.nav.no/graphql")
    )

    @BeforeEach
    fun startup() {
        service.initMetrics()
    }

    @Test
    fun hentPerson_virkerSomForventet() {
        val person = service.hentPerson(NorskIdent("25078521492"))
        println(person.toString())
        assertNotNull(person?.navn)
    }

    @Test
    fun harAdressebeskyttelse_virkerSomForventet() {
        val fnr = listOf("11067122781", "09035225916", "22117320034")
        val gradering = listOf(AdressebeskyttelseGradering.STRENGT_FORTROLIG, AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND)

        val harAdressebeskyttelse = service.harAdressebeskyttelse(fnr, gradering)

        assertFalse(harAdressebeskyttelse)
    }

    @Test
    fun hentAktorId() {
        assertNotNull(service.hentAktorId("11067122781"))
        assertNotNull(service.hentAktorId("09035225916"))
        assertNotNull(service.hentAktorId("22117320034"))
    }

    @Test
    fun sokPerson() {
//        P2000
//        64045349924 - KARAFFEL TUNGSINDIG
//
//        20035325957 - KARAFFEL KRAFTIG

        val sokKriterie = SokKriterier(
            fornavn = "TUNGSINDIG",
            etternavn = "KARAFFEL",
            foedselsdato = LocalDate.of(1953, 4, 24)
        )

        val result = service.sokPerson(sokKriterie)
        assertEquals("64045349924", result.firstOrNull { it.gruppe == IdentGruppe.FOLKEREGISTERIDENT }?.ident)


        val sokKriterie2 = SokKriterier(
            fornavn = "KRAFTIG",
            etternavn = "KARAFFEL",
            foedselsdato = LocalDate.of(1953, 3, 20)
        )

        val result2 = service.sokPerson(sokKriterie2)
        assertEquals("20035325957", result2.firstOrNull { it.gruppe == IdentGruppe.FOLKEREGISTERIDENT }?.ident)

    }

}
