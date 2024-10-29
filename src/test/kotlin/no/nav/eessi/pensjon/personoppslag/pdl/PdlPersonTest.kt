package no.nav.eessi.pensjon.personoppslag.pdl

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.personoppslag.pdl.model.Endring
import no.nav.eessi.pensjon.personoppslag.pdl.model.Endringstype
import no.nav.eessi.pensjon.personoppslag.pdl.model.Familierelasjonsrolle
import no.nav.eessi.pensjon.personoppslag.pdl.model.GeografiskTilknytningResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentPersonResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdenterResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.KjoennType
import no.nav.eessi.pensjon.personoppslag.pdl.model.KontaktadresseType
import no.nav.eessi.pensjon.personoppslag.pdl.model.Metadata
import no.nav.eessi.pensjon.personoppslag.pdl.model.Navn
import no.nav.eessi.pensjon.personoppslag.pdl.model.NorskIdent
import no.nav.eessi.pensjon.personoppslag.pdl.model.PdlPerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.Sivilstandstype
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class PdlPersonTest {

    private val mockPersonClient: PersonClient = mockk(relaxed = true)
    private val mockPersonService = PersonService(mockPersonClient)
    private val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @AfterEach
    fun after() {
        clearAllMocks()
    }

    private fun mockMeta(registrert: LocalDateTime = LocalDateTime.of(2010, 4,1, 13, 23, 10)): Metadata {
        return Metadata(
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

    private fun hentPersonFraFil(hentPersonfil: String): PdlPerson? {
        val response = mapper.readValue(hentPersonfil, HentPersonResponse::class.java)
        val emptyResponseJson = """
            {
              "data": null,
              "errors": null
            }
        """.trimIndent()
        val identResponse = mapper.readValue(emptyResponseJson, IdenterResponse::class.java)
        val geoResponse = mapper.readValue(emptyResponseJson, GeografiskTilknytningResponse::class.java)

        every { mockPersonClient.hentPerson( any()) } returns response
        every { mockPersonClient.hentIdenter (any()) } returns identResponse
        every { mockPersonClient.hentGeografiskTilknytning (any()) }  returns geoResponse

        return mockPersonService.hentPerson(NorskIdent("2"))
    }


    @Test
    fun `hentPerson med manglende relatertPersonsIdent skal fortsatt gi gyldig resultat`() {
        val json = javaClass.getResource("/hentPersonFamilieRelasjonUtenIdent.json").readText()
        var person = hentPersonFraFil(json)
        assertEquals(Familierelasjonsrolle.BARN, person?.forelderBarnRelasjon?.get(0)?.relatertPersonsRolle)
        assertNull(person?.forelderBarnRelasjon?.get(0)?.relatertPersonsIdent)
    }



    @Test
    fun `hentPerson med data i json deserialisering`() {
        val json = javaClass.getResource("/hentPerson.json").readText()
        val person = hentPersonFraFil(json)

        val navn = person?.navn
        assertEquals("BLÅ", navn?.fornavn)
        assertEquals("STAUDE", navn?.etternavn)

        val foedsel = person?.foedsel
        assertEquals(LocalDate.of(1985, 1, 1), foedsel?.foedselsdato)
        assertNotNull(foedsel?.folkeregistermetadata)

        val kjoenn = person?.kjoenn
        assertEquals(KjoennType.KVINNE, kjoenn?.kjoenn)
        assertNotNull(kjoenn?.folkeregistermetadata)

        val sivilstand = person?.sivilstand?.maxByOrNull { it.gyldigFraOgMed!! }!!
        assertEquals(Sivilstandstype.GIFT, sivilstand.type)
        assertEquals(LocalDate.of(2010, 1, 25), sivilstand.gyldigFraOgMed)
        assertEquals("03128222382", sivilstand.relatertVedSivilstand)

        val bostedsadresse = person.bostedsadresse
        assertEquals("SANNERGATA", bostedsadresse?.vegadresse?.adressenavn)

    }

    @Test
    fun `hentPerson med flere navn data i json deserialisering`() {
        val json = javaClass.getResource("/hentPersonMedFlereNavn.json").readText()
        val person = hentPersonFraFil(json)

        val navn = person?.navn
        assertEquals("BLÅ", navn?.fornavn)
        assertEquals("STAUDE", navn?.etternavn)

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
    fun `hentPerson Utlandadresse fra opphold`() {
        val json = javaClass.getResource("/hentUtlandPerson.json").readText()
        val person = hentPersonFraFil(json)

        val navn = person?.navn
        assertEquals("BLÅ", navn?.fornavn)
        assertEquals("STAUDE", navn?.etternavn)


        assertNull(person?.bostedsadresse)

        val opphold = person?.oppholdsadresse
        val utlandadresse = opphold?.utenlandskAdresse

        assertEquals(LocalDateTime.of(2021, 2, 15, 10, 41, 2), opphold?.metadata?.sisteRegistrertDato())

        assertEquals("SWE", utlandadresse?.landkode)
        assertEquals("GUBBAN GATA 21", utlandadresse?.adressenavnNummer)
        assertEquals("GØTAN", utlandadresse?.bySted)
        assertEquals("1021", utlandadresse?.bygningEtasjeLeilighet)
        assertEquals("3201", utlandadresse?.postboksNummerNavn)
        assertEquals("S-21233", utlandadresse?.postkode)
        assertEquals("SYDAN-SE", utlandadresse?.regionDistriktOmraade)

    }

    @Test
    fun `hentPerson med kontaktadresse med utenlandsk adresse i fritt format`() {
        val json = javaClass.getResource("/hentPersonUtlandMedKontaktAdresse.json").readText()
        val person = hentPersonFraFil(json)

        val navn = person?.navn
        assertEquals("TVILSOM", navn?.fornavn)
        assertEquals("KNOTT", navn?.etternavn)

        assertNull(person?.bostedsadresse)

        val opphold = person?.oppholdsadresse
        val utlandadresse = opphold?.utenlandskAdresse

        assertEquals(LocalDateTime.of(2021, 2, 16, 14, 53, 6), opphold?.metadata?.sisteRegistrertDato())

        assertEquals("TUV", utlandadresse?.landkode)
        assertEquals("1KOLEJOWA 6/5, 18-500 KOLNO, CAPITAL WEST 3000", utlandadresse?.adressenavnNummer)

        val kontaktadresse = person?.kontaktadresse
        val utenlandsadresseFrittformat = kontaktadresse?.utenlandskAdresseIFrittFormat

        assertEquals(KontaktadresseType.Utland, kontaktadresse?.type)
        assertNull(kontaktadresse?.utenlandskAdresse)
        assertNull(kontaktadresse?.vegadresse)
        assertEquals("1KOLEJOWA 6/5", utenlandsadresseFrittformat?.adresselinje1)
        assertEquals("18-500 KOLNO", utenlandsadresseFrittformat?.adresselinje2)
        assertEquals("CAPITAL WEST 3000", utenlandsadresseFrittformat?.adresselinje3)
        assertEquals("TUV", utenlandsadresseFrittformat?.landkode)
    }

    @Test
    fun `hentPerson med KontaktinformasjonForDoedsboAdresse - advokat`() {
        val json = javaClass.getResource("/hentPersonMedKontaktAdresseDoedsbo-Advokat.json")!!.readText()
        val person = hentPersonFraFil(json)
        val adresse = person?.kontaktinformasjonForDoedsbo?.adresse

        assertEquals("adresselinje1", adresse!!.adresselinje1)
        assertEquals("adresselinje2", adresse.adresselinje2)
        assertEquals("SWE", adresse.landkode)
        assertEquals("3123", adresse.postnummer)
        assertEquals("POSTSTEDSETSNAVN", adresse.poststedsnavn)

        val advokatSomKontakt = person.kontaktinformasjonForDoedsbo!!.advokatSomKontakt!!
        assertEquals("Arve", advokatSomKontakt.personnavn.fornavn)
        assertEquals("Bjørn", advokatSomKontakt.personnavn.mellomnavn)
        assertEquals("Stein", advokatSomKontakt.personnavn.etternavn)
        assertEquals("Stein Gale Advokater", advokatSomKontakt.organisasjonsnavn)
    }

    @Test
    fun `hentPerson med KontaktinformasjonForDoedsboAdresse - person`() {
        val json = javaClass.getResource("/hentPersonMedKontaktAdresseDoedsbo-Person.json")!!.readText()
        val person = hentPersonFraFil(json)
        val personSomKontakt = person!!.kontaktinformasjonForDoedsbo!!.personSomKontakt!!
        assertEquals("12345678910", personSomKontakt.identifikasjonsnummer)
    }

    @Test
    fun `hentPerson med KontaktinformasjonForDoedsboAdresse - organisasjon`() {
        val json = javaClass.getResource("/hentPersonMedKontaktAdresseDoedsbo-Organisasjon.json")!!.readText()
        val person = hentPersonFraFil(json)
        val organisasjonSomKontakt = person!!.kontaktinformasjonForDoedsbo!!.organisasjonSomKontakt!!
        assertEquals("Hvite", organisasjonSomKontakt.kontaktperson!!.fornavn)
        assertEquals("Blomster", organisasjonSomKontakt.kontaktperson!!.etternavn)
        assertEquals("ABC Vi Fikser Arven", organisasjonSomKontakt.organisasjonsnavn)
    }


    @Test
    fun `hentPerson kjoenn og foedsel and konvert`() {
        val json = javaClass.getResource("/hentPerson2Kjoenn.json").readText()
        val person = hentPersonFraFil(json)

        val vegadresse = person?.bostedsadresse?.vegadresse

        assertNull(person?.bostedsadresse?.utenlandskAdresse)

        assertEquals(null, vegadresse?.husbokstav)
        assertEquals("15", vegadresse?.husnummer)
        assertEquals("3183", vegadresse?.postnummer)
        assertEquals("STASJONSHAGEN", vegadresse?.adressenavn)
        assertEquals("3801", vegadresse?.kommunenummer)
        assertEquals(null, vegadresse?.bydelsnummer)

        val navn = person?.navn

        assertEquals("HEST", navn?.etternavn)
        assertEquals("ÅPENHJERTIG", navn?.fornavn)

        val foedsel = person?.foedsel

        assertEquals(LocalDate.of(1974, 3 , 17), foedsel?.foedselsdato)

        val kjoenn = person?.kjoenn

        assertEquals(KjoennType.MANN, kjoenn?.kjoenn)

    }

    @Test
    fun `hentPerson and konvert`() {
        val json = javaClass.getResource("/hentPerson2.json").readText()
        val person = hentPersonFraFil(json)

        val vegadresse = person?.bostedsadresse?.vegadresse

        assertNull(person?.bostedsadresse?.utenlandskAdresse)
        assertEquals(null, vegadresse?.husbokstav)
        assertEquals("15", vegadresse?.husnummer)
        assertEquals("3183", vegadresse?.postnummer)
        assertEquals("STASJONSHAGEN", vegadresse?.adressenavn)
        assertEquals("3801", vegadresse?.kommunenummer)
        assertEquals(null, vegadresse?.bydelsnummer)

        val navn = person?.navn

        assertEquals("HEST", navn?.etternavn)
        assertEquals("ÅPENHJERTIG", navn?.fornavn)

        val foedsel = person?.foedsel

        assertEquals(LocalDate.of(1974, 3 , 17), foedsel?.foedselsdato)

        val kjoenn = person?.kjoenn

        assertEquals(KjoennType.UKJENT, kjoenn?.kjoenn)

    }


}
