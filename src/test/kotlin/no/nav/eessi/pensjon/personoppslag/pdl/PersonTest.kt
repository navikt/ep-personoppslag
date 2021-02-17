package no.nav.eessi.pensjon.personoppslag.pdl

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.eessi.pensjon.personoppslag.pdl.model.Endring
import no.nav.eessi.pensjon.personoppslag.pdl.model.Endringstype
import no.nav.eessi.pensjon.personoppslag.pdl.model.KjoennType
import no.nav.eessi.pensjon.personoppslag.pdl.model.Metadata
import no.nav.eessi.pensjon.personoppslag.pdl.model.Navn
import no.nav.eessi.pensjon.personoppslag.pdl.model.PersonResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.Sivilstandstype
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class PersonTest {

    private val mapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())

    private fun mockMeta(registrert: LocalDate = LocalDate.of(2010, 4,1)): Metadata {
        return no.nav.eessi.pensjon.personoppslag.pdl.model.Metadata(
            endringer = listOf(Endring(
                "TEST",
                registrert,
                "DOLLY",
                "KAY",
                Endringstype.OPPRETT
            )),
            historisk = false,
            master = "TEST",
            opplysningsId = "31231-123123"
        )
    }

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

        val bostedsadresse = hentPerson.bostedsadresse
            .filterNot { it.gyldigFraOgMed == null }
            .maxBy { it.gyldigFraOgMed!! }

        assertEquals("SANNERGATA", bostedsadresse?.vegadresse?.adressenavn)

    }

    @Test
    fun sammensattNavn() {
        val navn = Navn("Fornavn", null, "Etternavn", null, null, null, mockMeta())
        val fultNavn = Navn("Fornavn", "Mellom", "Etternavn", null, null, null, mockMeta())

        assertEquals("Fornavn Etternavn", navn.sammensattNavn)
        assertEquals("Fornavn Mellom Etternavn", fultNavn.sammensattNavn)
    }

    @Test
    fun sammensattEtterNavn() {
        val navn = Navn("Fornavn", null, "Etternavn", null, null, null, mockMeta())
        val fultNavn = Navn("Fornavn", "Mellom", "Etternavn", null, null, null, mockMeta())

        assertEquals("Etternavn Fornavn", navn.sammensattEtterNavn)
        assertEquals("Etternavn Fornavn Mellom", fultNavn.sammensattEtterNavn)
    }

    @Test
    fun hentUtlandPerson_deserialisering() {
        val json = javaClass.getResource("/hentUtlandPerson.json").readText()
        val response = mapper.readValue(json, PersonResponse::class.java)

        val hentPerson = response.data?.hentPerson!!

        val navn = hentPerson.navn.firstOrNull()!!
        assertEquals("BLÅ", navn.fornavn)
        assertEquals("STAUDE", navn.etternavn)

        val bosted = hentPerson.bostedsadresse.firstOrNull()
        val utlandadresse = bosted?.utenlandskAdresse

        assertEquals("SWE", utlandadresse?.landkode)
        assertEquals("GUBBAN GATA 21", utlandadresse?.adressenavnNummer)
        assertEquals("GØTAN", utlandadresse?.bySted)
        assertEquals("1021", utlandadresse?.bygningEtasjeLeilighet)
        assertEquals("3201", utlandadresse?.postboksNummerNavn)
        assertEquals("S-21233", utlandadresse?.postkode)
        assertEquals("SYDAN-SE", utlandadresse?.regionDistriktOmraade)

    }

    @Test
    fun `hentPerson kjoenn og foedsel and konvert`() {
        val json = javaClass.getResource("/hentPerson2Kjoenn.json").readText()
        val response = mapper.readValue(json, PersonResponse::class.java)

        val hentPerson = response.data?.hentPerson!!

        val person = PersonService.konverterTilPerson(
            hentPerson,
            emptyList(),
            null
        )

        val vegadresse = person.bostedsadresse?.vegadresse

        assertNull(person.bostedsadresse?.utenlandskAdresse)
        assertEquals(null, vegadresse?.husbokstav)
        assertEquals("15", vegadresse?.husnummer)
        assertEquals("3183", vegadresse?.postnummer)
        assertEquals("STASJONSHAGEN", vegadresse?.adressenavn)
        assertEquals("3801", vegadresse?.kommunenummer)
        assertEquals(null, vegadresse?.bydelsnummer)

        val navn = person.navn

        assertEquals("HEST", navn?.etternavn)
        assertEquals("ÅPENHJERTIG", navn?.fornavn)

        val foedsel = person.foedsel

        assertEquals(LocalDate.of(1974, 3 , 17), foedsel?.foedselsdato)

        val kjoenn = person.kjoenn

        assertEquals(KjoennType.MANN, kjoenn?.kjoenn)

    }

    @Test
    fun `hentPerson and konvert`() {
        val json = javaClass.getResource("/hentPerson2.json").readText()
        val response = mapper.readValue(json, PersonResponse::class.java)

        val hentPerson = response.data?.hentPerson!!

        val person = PersonService.konverterTilPerson(
            hentPerson,
            emptyList(),
            null
        )

        val vegadresse = person.bostedsadresse?.vegadresse

        assertNull(person.bostedsadresse?.utenlandskAdresse)
        assertEquals(null, vegadresse?.husbokstav)
        assertEquals("15", vegadresse?.husnummer)
        assertEquals("3183", vegadresse?.postnummer)
        assertEquals("STASJONSHAGEN", vegadresse?.adressenavn)
        assertEquals("3801", vegadresse?.kommunenummer)
        assertEquals(null, vegadresse?.bydelsnummer)

        val navn = person.navn

        assertEquals("HEST", navn?.etternavn)
        assertEquals("ÅPENHJERTIG", navn?.fornavn)

        val foedsel = person.foedsel

        assertEquals(LocalDate.of(1974, 3 , 17), foedsel?.foedselsdato)

        val kjoenn = person.kjoenn

        assertEquals(KjoennType.UKJENT, kjoenn?.kjoenn)

    }


}
