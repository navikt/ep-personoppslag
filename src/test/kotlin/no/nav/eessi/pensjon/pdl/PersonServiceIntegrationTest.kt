package no.nav.eessi.pensjon.pdl

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.security.sts.STSService
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
        } returns ""
    }

    private val mockClient = PdlConfiguration(mockStsService)
            .pdlRestTemplate(RestTemplateBuilder())

    /**
     * Use local port forwarding using kubectl and nais
     *
     * Example: kubectl port-forward svc/pdl-api 8089:80
     */
    private val service = PersonService(
            PersonClient(mockClient, "http://localhost:8089/graphql"),
            mockStsService
    )

    @Test
    fun hentPerson_virkerSomForventet() {
        val person = service.hentPerson("11067122781")

        assertNotNull(person?.navn)
    }

}
