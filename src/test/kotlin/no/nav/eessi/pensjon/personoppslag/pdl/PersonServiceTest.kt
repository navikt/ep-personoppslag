package no.nav.eessi.pensjon.personoppslag.pdl

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.personoppslag.pdl.model.Adressebeskyttelse
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseBolkPerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseGradering
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelsePerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.AktoerId
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentAdressebeskyttelse
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentIdenter
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentGruppe
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentGruppen
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentInformasjon
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdenterDataResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdenterResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.Navn
import no.nav.eessi.pensjon.personoppslag.pdl.model.NorskIdent
import no.nav.eessi.pensjon.personoppslag.pdl.model.PdlPerson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PersonServiceTest {

    private val client = mockk<PersonClient>()

    private val service: PersonService = PersonService(client)

    @Test
    fun hentPerson() {
        val pdlPerson = PdlPerson(
                adressebeskyttelse = emptyList(),
                navn = listOf(Navn("Fornavn", "Mellomnavn", "Etternavn")),
                statsborgerskap = emptyList(),
                foedsel = emptyList()
        )

        every { client.hentPerson(any()) } returns PersonResponse(HentPerson(pdlPerson))

        val resultat = service.hentPerson("12345")!!

        val navn = resultat.navn!!.first()
        assertEquals("Fornavn", navn.fornavn)
        assertEquals("Mellomnavn", navn.mellomnavn)
        assertEquals("Etternavn", navn.etternavn)
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
                IdentInformasjon("1", IdentGruppe.AKTORID, false),
                IdentInformasjon("2", IdentGruppe.FOLKEREGISTERIDENT, false),
                IdentInformasjon("3", IdentGruppe.NPID, false)
        )

        every { client.hentIdenter(any()) } returns IdenterResponse(IdenterDataResponse(HentIdenter(identer)))

        val resultat = service.hentIdenter("12345")

        assertEquals(3, resultat.size)
    }

    @Test
    fun hentAktorId() {
        val identer = listOf(
                IdentInformasjon("1", IdentGruppe.AKTORID, false),
                IdentInformasjon("2", IdentGruppe.FOLKEREGISTERIDENT, false),
                IdentInformasjon("3", IdentGruppe.NPID, false)
        )

        every { client.hentAktorId(any()) } returns IdenterResponse(IdenterDataResponse(HentIdenter(identer)))

        val aktorId = service.hentAktorId("12345")

        assertEquals("1", aktorId)
    }

    @Test
    fun hentNorskIdent() {
        val identer = listOf(
            IdentInformasjon("1", IdentGruppe.AKTORID, false),
            IdentInformasjon("2", IdentGruppe.FOLKEREGISTERIDENT, false),
            IdentInformasjon("3", IdentGruppe.NPID, false)
        )

        every { client.hentAktorId(any()) } returns IdenterResponse(IdenterDataResponse(HentIdenter(identer)))

        val fnr = service.hentGjeldendeIdent (IdentGruppen.NorskIdent, AktoerId("1"))?.id
        assertEquals("2", fnr)

        val aktoer = service.hentGjeldendeIdent (IdentGruppen.AktoerId, NorskIdent("2"))?.id
        assertEquals("1", aktoer)

    }


    @Test
    fun hentAktorId_graphqlErrorThrowsException() {
        val errors = listOf(ResponseError(message = "unauthorized"))

        every { client.hentIdenter(any()) } returns IdenterResponse(data = null, errors = errors)

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