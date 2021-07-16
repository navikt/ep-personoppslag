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
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import java.time.LocalDate

internal class PersonServiceIntegrationTest {

    /**
     * Paste valid token
     */
    private val mockStsService = mockk<STSService> {
        every {
            getSystemOidcToken()
        } returns "eyJraWQiOiIwYmVkY2JmYy03OWFjLTQxNTItYmZiYy04MzRkM2I2MzViMWMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzcnZlZXNzaXBlbnNqb24iLCJhdWQiOlsic3J2ZWVzc2lwZW5zam9uIiwicHJlcHJvZC5sb2NhbCJdLCJ2ZXIiOiIxLjAiLCJuYmYiOjE2MjY0MjA0MTksImF6cCI6InNydmVlc3NpcGVuc2pvbiIsImlkZW50VHlwZSI6IlN5c3RlbXJlc3N1cnMiLCJhdXRoX3RpbWUiOjE2MjY0MjA0MTksImlzcyI6Imh0dHBzOlwvXC9zZWN1cml0eS10b2tlbi1zZXJ2aWNlLm5haXMucHJlcHJvZC5sb2NhbCIsImV4cCI6MTYyNjQyNDAxOSwiaWF0IjoxNjI2NDIwNDE5LCJqdGkiOiIxYzUxNTQ2MS0wYjg2LTRmNTgtODkxNS1kM2I3NmIxZGRmNDYifQ.JGGDNhQkfDFAdlJEpuffZml9urbCLvCbMkvtb3pg7fj50U5OtQ-lV6nysE1kBYwpf8Z0f_DTnIy9bhWyUTK048Geeeaf-AHtNLCJ3RDZDZgm31519gTvCdDt6W328BDPmG8cS9mqAv9PG7odBI9Em1olPjpXmXu4TdpLR7sktaY5p1xGa7sSE-nUFHk7gJf9h0RFcGBdGSqE1qzsV0ROnBKFA4A05JBD5d-zbQWQSpxbv0mbKEE8kNwyWcJho860Xtr6kd_AW8uFCy19y6lhjBXmtId2-M6LLY2S88ekmVMHkkMRh280BTkDU6qpoxcGN8bEQRHhzYOsDVX9zc0g1Q"
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
