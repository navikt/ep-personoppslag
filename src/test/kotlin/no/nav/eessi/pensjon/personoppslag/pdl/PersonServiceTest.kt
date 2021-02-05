package no.nav.eessi.pensjon.personoppslag.pdl

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.personoppslag.pdl.model.Adressebeskyttelse
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseBolkPerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseGradering
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelsePerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.AktoerId
import no.nav.eessi.pensjon.personoppslag.pdl.model.Bostedsadresse
import no.nav.eessi.pensjon.personoppslag.pdl.model.Doedsfall
import no.nav.eessi.pensjon.personoppslag.pdl.model.ErrorExtension
import no.nav.eessi.pensjon.personoppslag.pdl.model.Familierelasjonsrolle
import no.nav.eessi.pensjon.personoppslag.pdl.model.Familierelasjon
import no.nav.eessi.pensjon.personoppslag.pdl.model.Foedsel
import no.nav.eessi.pensjon.personoppslag.pdl.model.Folkeregistermetadata
import no.nav.eessi.pensjon.personoppslag.pdl.model.GeografiskTilknytning
import no.nav.eessi.pensjon.personoppslag.pdl.model.GeografiskTilknytningResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.GeografiskTilknytningResponseData
import no.nav.eessi.pensjon.personoppslag.pdl.model.GtType
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentAdressebeskyttelse
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentIdenter
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentPerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentGruppe
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentInformasjon
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentType
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdenterDataResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdenterResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.Kjoenn
import no.nav.eessi.pensjon.personoppslag.pdl.model.KjoennType
import no.nav.eessi.pensjon.personoppslag.pdl.model.Navn
import no.nav.eessi.pensjon.personoppslag.pdl.model.NorskIdent
import no.nav.eessi.pensjon.personoppslag.pdl.model.Npid
import no.nav.eessi.pensjon.personoppslag.pdl.model.Oppholdsadresse
import no.nav.eessi.pensjon.personoppslag.pdl.model.PersonResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.PersonResponseData
import no.nav.eessi.pensjon.personoppslag.pdl.model.ResponseError
import no.nav.eessi.pensjon.personoppslag.pdl.model.Sivilstand
import no.nav.eessi.pensjon.personoppslag.pdl.model.Sivilstandstype
import no.nav.eessi.pensjon.personoppslag.pdl.model.Statsborgerskap
import no.nav.eessi.pensjon.personoppslag.pdl.model.Vegadresse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime

@TestInstance(Lifecycle.PER_CLASS)
internal class PersonServiceTest {

    private val client = mockk<PersonClient>()

    private val service: PersonService = PersonService(client)

    @BeforeAll
    fun beforeAll() {
        service.initMetrics()
    }

    @Test
    fun hentPerson() {
        val pdlPerson = createHentPerson(
                adressebeskyttelse = listOf(Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT)),
                navn = listOf(Navn("Fornavn", "Mellomnavn", "Etternavn"))
        )

         val identer = listOf(
                IdentInformasjon("25078521492", IdentGruppe.FOLKEREGISTERIDENT),
                IdentInformasjon("100000000000053", IdentGruppe.AKTORID)
        )

        val gt = GeografiskTilknytning(GtType.KOMMUNE, "0301", null, null)

        every { client.hentPerson(any()) } returns PersonResponse(PersonResponseData(pdlPerson))
        every { client.hentIdenter(any()) } returns IdenterResponse(IdenterDataResponse(HentIdenter(identer)))
        every { client.hentGeografiskTilknytning(any()) } returns GeografiskTilknytningResponse(GeografiskTilknytningResponseData(gt))

        val resultat = service.hentPerson(NorskIdent("12345"))

        val navn = resultat!!.navn!!
        assertEquals("Fornavn", navn.fornavn)
        assertEquals("Mellomnavn", navn.mellomnavn)
        assertEquals("Etternavn", navn.etternavn)

        assertEquals(gt, resultat.geografiskTilknytning)

        assertEquals(1, resultat.adressebeskyttelse!!.size)
        assertEquals(2, resultat.identer.size)
    }

    @Test
    fun hentAltPerson() {
        val pdlPerson = HentPerson(
            adressebeskyttelse = listOf(Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT)),
            bostedsadresse = listOf(
                Bostedsadresse(
                    LocalDateTime.of(2020, 10, 5, 10,5,2),
                    LocalDateTime.of(2030, 10, 5, 10, 5, 2),
                    Vegadresse("TESTVEIEN","1020","A","0234"),
                    null
                )
            ),
            oppholdsadresse = emptyList(),
            navn = listOf(Navn("Fornavn", "Mellomnavn", "Etternavn")),
            statsborgerskap = listOf(Statsborgerskap("NOR", LocalDate.of(2010, 7,7), LocalDate.of(2020, 10, 10))),
            foedsel = listOf(Foedsel(LocalDate.of(2000,10,3), "NOR", "OSLO", Folkeregistermetadata(LocalDateTime.of(2020, 10, 5, 10,5,2)))),
            kjoenn = listOf(Kjoenn(KjoennType.KVINNE, Folkeregistermetadata(LocalDateTime.of(2020, 10, 5, 10,5,2)))),
            doedsfall = listOf(Doedsfall(LocalDate.of(2020, 10,10), Folkeregistermetadata(LocalDateTime.of(2020, 10, 5, 10,5,2))) ),
            familierelasjoner = listOf(Familierelasjon(relatertPersonsIdent = "101010", relatertPersonsRolle = Familierelasjonsrolle.BARN, minRolleForPerson = Familierelasjonsrolle.MOR)),
            sivilstand = listOf(Sivilstand(Sivilstandstype.GIFT, LocalDate.of(2010, 10,10), "1020203010"))
        )

        val identer = listOf(
            IdentInformasjon("25078521492", IdentGruppe.FOLKEREGISTERIDENT),
            IdentInformasjon("100000000000053", IdentGruppe.AKTORID)
        )

        val gt = GeografiskTilknytning(GtType.KOMMUNE, "0301", null, null)

        every { client.hentPerson(any()) } returns PersonResponse(PersonResponseData(pdlPerson))
        every { client.hentIdenter(any()) } returns IdenterResponse(IdenterDataResponse(HentIdenter(identer)))
        every { client.hentGeografiskTilknytning(any()) } returns GeografiskTilknytningResponse(GeografiskTilknytningResponseData(gt))

        val resultat = service.hentPerson(NorskIdent("12345"))

        val navn = resultat!!.navn!!
        assertEquals("Fornavn", navn.fornavn)
        assertEquals("Mellomnavn", navn.mellomnavn)
        assertEquals("Etternavn", navn.etternavn)

        val vegadresse = resultat.bostedsadresse!!.vegadresse
        assertEquals("TESTVEIEN", vegadresse?.adressenavn)
        assertEquals("1020", vegadresse?.husnummer)
        assertEquals("A", vegadresse?.husbokstav)
        assertEquals("0234", vegadresse?.postnummer)

        assertEquals("NOR", resultat.statsborgerskap.lastOrNull()?.land)

        assertEquals(LocalDate.of(2000,10,3), resultat.foedsel?.foedselsdato)
        assertEquals("NOR", resultat?.foedsel?.foedeland)
        assertEquals("OSLO", resultat?.foedsel?.foedested)

        assertEquals(KjoennType.KVINNE, resultat.kjoenn?.kjoenn)

        assertEquals(LocalDate.of(2020, 10,10), resultat.doedsfall?.doedsdato)
        assertEquals(true, resultat.erDoed())

        assertEquals("101010", resultat.familierelasjoner.lastOrNull()?.relatertPersonsIdent)
        assertEquals(Familierelasjonsrolle.BARN, resultat.familierelasjoner.lastOrNull()?.relatertPersonsRolle)
        assertEquals(Familierelasjonsrolle.MOR, resultat.familierelasjoner.lastOrNull()?.minRolleForPerson)

        assertEquals(Sivilstandstype.GIFT, resultat.sivilstand?.lastOrNull()?.type)
        assertEquals("1020203010", resultat.sivilstand?.lastOrNull()?.relatertVedSivilstand)
        assertEquals(LocalDate.of(2010, 10,10), resultat.sivilstand?.lastOrNull()?.gyldigFraOgMed)

        assertEquals(gt, resultat.geografiskTilknytning)

        assertEquals(1, resultat.adressebeskyttelse!!.size)
        assertEquals(2, resultat.identer.size)
    }

    @Test
    fun kjoenn_sistGyldigeVerdiBlirValgt() {
        val kjoennListe = listOf(
                Kjoenn(KjoennType.MANN, Folkeregistermetadata(LocalDateTime.now().minusDays(10))),
                Kjoenn(KjoennType.UKJENT, null),
                Kjoenn(KjoennType.KVINNE, Folkeregistermetadata(LocalDateTime.now()))
        )

        val person = createHentPerson(kjoenn = kjoennListe)

        every { client.hentPerson(any()) } returns PersonResponse(PersonResponseData(person))
        every { client.hentIdenter(any()) } returns IdenterResponse(IdenterDataResponse(HentIdenter(emptyList())))
        every { client.hentGeografiskTilknytning(any()) } returns GeografiskTilknytningResponse(null, null)

        val resultat = service.hentPerson(NorskIdent("12345"))

        assertEquals(KjoennType.KVINNE, resultat!!.kjoenn!!.kjoenn)
    }

    @Test
    fun doedsfall_sistGyldigeVerdiBlirValgt() {
        val now = LocalDate.now()

        val doedsfallListe = listOf(
                Doedsfall(LocalDate.now(), Folkeregistermetadata(null)), // Mangler gyldig-dato
                Doedsfall(LocalDate.of(2020, 10, 1), null), // Mangler folkereg.-metadata
                Doedsfall(null, Folkeregistermetadata(LocalDateTime.now())), // Mangler doedsfall-dato
                Doedsfall(LocalDate.of(2019, 8, 5), Folkeregistermetadata(LocalDateTime.now().minusDays(50))), // 50 dager gammel gyldighet
                Doedsfall(now, Folkeregistermetadata(LocalDateTime.now())) // Markert som gyldig fra nå (FORVENTET RESULTAT)
        )

        val person = createHentPerson(doedsfall = doedsfallListe)

        every { client.hentPerson(any()) } returns PersonResponse(PersonResponseData(person))
        every { client.hentIdenter(any()) } returns IdenterResponse(IdenterDataResponse(HentIdenter(emptyList())))
        every { client.hentGeografiskTilknytning(any()) } returns GeografiskTilknytningResponse(null, null)

        val resultat = service.hentPerson(NorskIdent("12345"))!!

        assertEquals(now, resultat.doedsfall?.doedsdato)
    }


    @Test
    fun harAdressebeskyttelse_ingenTreff() {
        val personer = listOf(
                mockGradertPerson(AdressebeskyttelseGradering.UGRADERT),
                mockGradertPerson(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND),
                mockGradertPerson(AdressebeskyttelseGradering.FORTROLIG),
                mockGradertPerson(AdressebeskyttelseGradering.UGRADERT)
        )

        every { client.hentAdressebeskyttelse(any()) } returns AdressebeskyttelseResponse(HentAdressebeskyttelse(personer))

        val resultat = service.harAdressebeskyttelse(
                listOf("12345", "5555", "8585"),
                listOf(AdressebeskyttelseGradering.STRENGT_FORTROLIG)
        )

        assertFalse(resultat)
    }

    @Test
    fun harAdressebeskyttelse_harGradering() {
        val personer = listOf(
                mockGradertPerson(AdressebeskyttelseGradering.UGRADERT),
                mockGradertPerson(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND),
                mockGradertPerson(AdressebeskyttelseGradering.STRENGT_FORTROLIG),
                mockGradertPerson(AdressebeskyttelseGradering.FORTROLIG),
                mockGradertPerson(AdressebeskyttelseGradering.UGRADERT)
        )

        every { client.hentAdressebeskyttelse(any()) } returns AdressebeskyttelseResponse(HentAdressebeskyttelse(personer))

        val resultat = service.harAdressebeskyttelse(
                listOf("12345", "5555", "8585"),
                listOf(AdressebeskyttelseGradering.STRENGT_FORTROLIG)
        )

        assertTrue(resultat)
    }

    @Test
    fun hentIdenter() {
        val identer = listOf(
                IdentInformasjon("1", IdentGruppe.AKTORID),
                IdentInformasjon("2", IdentGruppe.FOLKEREGISTERIDENT),
                IdentInformasjon("3", IdentGruppe.NPID)
        )

        every { client.hentIdenter(any()) } returns IdenterResponse(IdenterDataResponse(HentIdenter(identer)))

        val resultat = service.hentIdenter(NorskIdent("12345"))

        assertEquals(3, resultat.size)
    }

    @Test
    fun hentAktorId() {
        val identer = listOf(
                IdentInformasjon("1", IdentGruppe.AKTORID),
                IdentInformasjon("2", IdentGruppe.FOLKEREGISTERIDENT),
                IdentInformasjon("3", IdentGruppe.NPID)
        )

        every { client.hentAktorId(any()) } returns IdenterResponse(IdenterDataResponse(HentIdenter(identer)))

        val aktorId = service.hentAktorId("12345")

        assertEquals(AktoerId("1"), aktorId)
    }

    @Test
    fun hentIdent() {
        val identer = listOf(
                IdentInformasjon("1", IdentGruppe.AKTORID),
                IdentInformasjon("2", IdentGruppe.FOLKEREGISTERIDENT),
                IdentInformasjon("3", IdentGruppe.NPID)
        )

        every { client.hentIdenter(any()) } returns IdenterResponse(IdenterDataResponse(HentIdenter(identer)))

        // Hente ut NorskIdent med AktørID
        val norskIdentFraAktorId = service.hentIdent(IdentType.NorskIdent, AktoerId("1"))
        assertEquals("2", norskIdentFraAktorId.id)

        // Hente ut NPID med AktørID
        val npidFraAktorId = service.hentIdent(IdentType.Npid, AktoerId("1"))
        assertEquals("3", npidFraAktorId.id)

        // Hente ut AktørID med NorskIdent
        val aktoeridFraNorskIdent = service.hentIdent(IdentType.AktoerId, NorskIdent("2"))
        assertEquals("1", aktoeridFraNorskIdent.id)

        // Hente ut NPID med NorskIdent
        val npidFraNorskIdent = service.hentIdent(IdentType.Npid, NorskIdent("2"))
        assertEquals("3", npidFraNorskIdent.id)

        // Hente ut AktørID med Npid
        val aktoeridFraNpid = service.hentIdent(IdentType.AktoerId, Npid("2"))
        assertEquals("1", aktoeridFraNpid.id)

        // Hente ut NorskIdent med Npid
        val norskIdentFraNpid = service.hentIdent(IdentType.NorskIdent, Npid("2"))
        assertEquals("2", norskIdentFraNpid.id)
    }

    @Test
    fun hentPerson_handleError() {
        val msg = "test message"
        val code = "test_code"

        val errors = listOf(ResponseError(msg, extensions = ErrorExtension(code, null, null)))

        every { client.hentPerson(any()) } returns PersonResponse(null, errors)

        val exception = assertThrows<PersonoppslagException> {
            service.hentPerson(NorskIdent("test"))
        }

        assertEquals("$code: $msg", exception.message)
    }

    @Test
    fun harAdressebeskyttelse_handleError() {
        val msg = "test message"
        val code = "test_code"

        val errors = listOf(ResponseError(msg, extensions = ErrorExtension(code, null, null)))

        every { client.hentAdressebeskyttelse(any()) } returns AdressebeskyttelseResponse(null, errors)

        val exception = assertThrows<PersonoppslagException> {
            service.harAdressebeskyttelse(listOf("1234"), listOf(AdressebeskyttelseGradering.FORTROLIG))
        }

        assertEquals("$code: $msg", exception.message)
    }

    @Test
    fun hentAktorId_handleError() {
        val msg = "test message"
        val code = "test_code"

        val errors = listOf(ResponseError(msg, extensions = ErrorExtension(code, null, null)))

        every { client.hentAktorId(any()) } returns IdenterResponse(data = null, errors = errors)

        val exception = assertThrows<PersonoppslagException> {
            service.hentAktorId("12345")
        }

        assertEquals("$code: $msg", exception.message)
    }

    @Test
    fun hentIdenter_handleError() {
        val msg = "test message"
        val code = "test_code"

        val errors = listOf(ResponseError(msg, extensions = ErrorExtension(code, null, null)))

        every { client.hentIdenter(any()) } returns IdenterResponse(data = null, errors = errors)

        val exception = assertThrows<PersonoppslagException> {
            service.hentIdenter(NorskIdent("12345"))
        }

        assertEquals("$code: $msg", exception.message)
    }

    @Test
    fun hentGeografiskTilknytning_handleError() {
        val msg = "test message"
        val code = "test_code"

        val errors = listOf(ResponseError(msg, extensions = ErrorExtension(code, null, null)))

        every { client.hentGeografiskTilknytning(any()) } returns GeografiskTilknytningResponse(data = null, errors = errors)

        val exception = assertThrows<PersonoppslagException> {
            service.hentGeografiskTilknytning(NorskIdent("12345"))
        }

        assertEquals("$code: $msg", exception.message)
    }

    private fun createHentPerson(
        adressebeskyttelse: List<Adressebeskyttelse> = emptyList(),
        bostedsadresse: List<Bostedsadresse> = emptyList(),
        oppholdsadresse: List<Oppholdsadresse> = emptyList(),
        navn: List<Navn> = emptyList(),
        statsborgerskap: List<Statsborgerskap> = emptyList(),
        foedsel: List<Foedsel> = emptyList(),
        kjoenn: List<Kjoenn> = emptyList(),
        doedsfall: List<Doedsfall> = emptyList(),
        familierelasjoner: List<Familierelasjon> = emptyList(),
        sivilstand: List<Sivilstand> = emptyList()
    ) = HentPerson(
            adressebeskyttelse, bostedsadresse, oppholdsadresse, navn, statsborgerskap, foedsel, kjoenn, doedsfall, familierelasjoner, sivilstand
    )

    private fun mockGradertPerson(gradering: AdressebeskyttelseGradering) =
            AdressebeskyttelseBolkPerson(
                    AdressebeskyttelsePerson(
                            adressebeskyttelse = listOf(Adressebeskyttelse(gradering))
                    )
            )
}