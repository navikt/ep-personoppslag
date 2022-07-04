package no.nav.eessi.pensjon.personoppslag.pdl.model

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HentPersonnavnResponseTest {

    private val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @Test
    fun `hentPersonnavn`() {
        val json = javaClass.getResource("/hentPersonnavn.json").readText()
        val response = mapper.readValue(json, HentPersonnavnResponse::class.java)

        assertEquals("STAUDE", response.data?.hentPerson?.navn?.first()?.etternavn)
    }

}
