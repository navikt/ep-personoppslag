package no.nav.eessi.pensjon.personoppslag.pdl

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseGradering
import no.nav.eessi.pensjon.security.sts.STSService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder

@Disabled
internal class PersonServiceIntegrationTest {

    /**
     * Paste valid token
     */
    private val mockStsService = mockk<STSService> {
        every {
            getSystemOidcToken()
        } returns "eyJraWQiOiJmYWM1YzdhZC02Y2RjLTQyMGQtOGE3OC1kYjIyMTcxZDJiOGUiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzcnZlZXNzaXBlbnNqb24iLCJhdWQiOlsic3J2ZWVzc2lwZW5zam9uIiwicHJlcHJvZC5sb2NhbCJdLCJ2ZXIiOiIxLjAiLCJuYmYiOjE2MTA1MjcyMTQsImF6cCI6InNydmVlc3NpcGVuc2pvbiIsImlkZW50VHlwZSI6IlN5c3RlbXJlc3N1cnMiLCJhdXRoX3RpbWUiOjE2MTA1MjcyMTQsImlzcyI6Imh0dHBzOlwvXC9zZWN1cml0eS10b2tlbi1zZXJ2aWNlLm5haXMucHJlcHJvZC5sb2NhbCIsImV4cCI6MTYxMDUzMDgxNCwiaWF0IjoxNjEwNTI3MjE0LCJqdGkiOiI1NmZlODUzMi01MTNmLTRiYTUtYWIxOC0wY2U4ZWMxNzA0YWQifQ.G70ztIbZbUyN5QI5hQdZGQwyiQQ3VtHRPr8PlIWoTl4ohHhfLRvBxWJIyADRMCPzcCLDusJUzSB5kst0UyT6P0hFoGgNEq2zXBYJUazyHa542_KGCkwSF3nVcB1M6PyoAdSmUxtprHiicKxpJK5kT2FDUpfTXpQyMtoTmyAkuiS1MTIM9eYWGr_qBXYlROThX2g3jQGoDqLgk_4R1k_uSIUqvyRE_15p4LUdvwEJl0Im17kk10LyljD-oOAbbiHi_LDNmAmyeUqhXi46bT6ZPzbnKsP3kdz_tDmnVnXsTq3EdD-f_Va1JT3SuE6peB8Q6GVyIziKqopKpYRTbI_4Ng"
    }

    private val mockClient = PdlConfiguration(mockStsService)
            .pdlRestTemplate(RestTemplateBuilder())

    /**
     * Use local port forwarding using kubectl and nais
     *
     * Example: kubectl port-forward svc/pdl-api 8089:80
     */
    private val service = PersonService(
            PersonClient(mockClient, "http://localhost:8089/graphql")
    )

    @Test
    fun hentPerson_virkerSomForventet() {
        val person = service.hentPerson("11067122781")

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

}
