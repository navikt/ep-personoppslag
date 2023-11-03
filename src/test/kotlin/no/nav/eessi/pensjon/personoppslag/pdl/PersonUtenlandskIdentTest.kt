package no.nav.eessi.pensjon.personoppslag.pdl

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.personoppslag.pdl.model.GeografiskTilknytningResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentPersonUidResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdenterResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.NorskIdent
import no.nav.eessi.pensjon.personoppslag.pdl.model.PersonUtenlandskIdent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class PersonUtenlandskIdentTest {

    private val mockPersonClient: PersonClient = mockk(relaxed = true)
    private val mockPersonService = PersonService(mockPersonClient)
    private val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @AfterEach
    fun after() {
        clearAllMocks()
    }

    @Test
    fun testforPersonMedUid() {
        val json = javaClass.getResource("/hentPersonUid.json").readText()
        val result = hentPersonMedUidFraFil(json)

        assertNotNull(result)
        assertEquals(1, result?.utenlandskIdentifikasjonsnummer?.size)
        assertEquals("12312312-234134134-14351345", result?.utenlandskIdentifikasjonsnummer?.firstOrNull()?.identifikasjonsnummer)

    }


    fun hentPersonMedUidFraFil(hentPersonfil: String): PersonUtenlandskIdent? {
        val response = mapper.readValue(hentPersonfil, HentPersonUidResponse::class.java)
        val emptyResponseJson = """
            {
              "data": null,
              "errors": null
            }
        """.trimIndent()
        val identResponse = mapper.readValue(emptyResponseJson, IdenterResponse::class.java)
        val geoResponse = mapper.readValue(emptyResponseJson, GeografiskTilknytningResponse::class.java)

        every { mockPersonClient.hentIdenter (any()) } returns identResponse
        every { mockPersonClient.hentGeografiskTilknytning (any()) }  returns geoResponse
        every { mockPersonClient.hentPersonUtenlandsIdent(any()) } returns response

        return mockPersonService.hentPersonUtenlandskIdent(NorskIdent("2"))
    }


}