package no.nav.eessi.pensjon.personoppslag.pdl

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.eessi.pensjon.personoppslag.pdl.model.KjoennType
import no.nav.eessi.pensjon.personoppslag.pdl.model.PersonResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.Sivilstandstype
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class PersonTest {

    private val mapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())

    @Test
    fun hentPerson_deserialisering() {
        val json = javaClass.getResource("/hentPerson.json").readText()
        val response = mapper.readValue(json, PersonResponse::class.java)

        val hentPerson = response.data?.hentPerson!!

        val navn = hentPerson.navn.firstOrNull()!!
        assertEquals("BLÅ", navn.fornavn)
        assertEquals("STAUDE", navn.etternavn)

        val foedsel = hentPerson.foedsel.maxBy { it.folkeregistermetadata!!.gyldighetstidspunkt!! }!!
        assertEquals(LocalDate.of(1985, 6, 11), foedsel.foedselsdato)
        assertNotNull(foedsel.folkeregistermetadata)

        val kjoenn = hentPerson.kjoenn.maxBy { it.folkeregistermetadata!!.gyldighetstidspunkt!! }!!
        assertEquals(KjoennType.KVINNE, kjoenn.kjoenn)
        assertNotNull(kjoenn.folkeregistermetadata)

        val sivilstand = hentPerson.sivilstand.maxBy { it.gyldigFraOgMed!! }!!
        assertEquals(Sivilstandstype.GIFT, sivilstand.type)
        assertEquals(LocalDate.of(2010, 1, 25), sivilstand.gyldigFraOgMed)
        assertEquals("03128222382", sivilstand.relatertVedSivilstand)
    }

}