package no.nav.eessi.pensjon.personoppslag.pdl

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.personoppslag.pdl.model.Adressebeskyttelse
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseBolkPerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseGradering
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelsePerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.AktoerId
import no.nav.eessi.pensjon.personoppslag.pdl.model.Folkeregisteridentifikator
import no.nav.eessi.pensjon.personoppslag.pdl.model.GeografiskTilknytning
import no.nav.eessi.pensjon.personoppslag.pdl.model.GeografiskTilknytningResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentAdressebeskyttelse
import no.nav.eessi.pensjon.personoppslag.pdl.model.GeografiskTilknytningResponseData
import no.nav.eessi.pensjon.personoppslag.pdl.model.GtType
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentIdenter
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentPerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentGruppe
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentInformasjon
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentType
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdenterDataResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdenterResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.Navn
import no.nav.eessi.pensjon.personoppslag.pdl.model.NorskIdent
import no.nav.eessi.pensjon.personoppslag.pdl.model.Npid
import no.nav.eessi.pensjon.personoppslag.pdl.model.PersonResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.PersonResponseData
import no.nav.eessi.pensjon.personoppslag.pdl.model.ResponseError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PersonServiceTest {

    private val client = mockk<PersonClient>()

    private val service: PersonService = PersonService(client)

    @Test
    fun hentPerson() {
        val pdlPerson = HentPerson(
                adressebeskyttelse = listOf(Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT)),
                bostedsadresse = emptyList(),
                oppholdsadresse = emptyList(),
                navn = listOf(Navn("Fornavn", "Mellomnavn", "Etternavn")),
                statsborgerskap = emptyList(),
                foedsel = emptyList()
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
    fun harAdressebeskyttelse() {
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

        assertEquals("1", aktorId)
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
    fun hentAktorId_graphqlErrorThrowsException() {
        val errors = listOf(ResponseError(message = "unauthorized"))

        every { client.hentAktorId(any()) } returns IdenterResponse(data = null, errors = errors)

        assertThrows<Exception> {
            service.hentAktorId("12345")
        }
    }

    private fun mockGradertPerson(gradering: AdressebeskyttelseGradering) =
            AdressebeskyttelseBolkPerson(
                    AdressebeskyttelsePerson(
                            adressebeskyttelse = listOf(Adressebeskyttelse(gradering))
                    )
            )
}