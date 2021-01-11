package no.nav.eessi.pensjon.pdl

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.security.sts.STSService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PersonServiceTest {

    private val client = mockk<PersonClient>()
    private val stsService = mockk<STSService>(relaxed = true)

    private val service: PersonService = PersonService(client, stsService)

    @Test
    fun hentPerson() {
        val pdlPerson = PdlPerson(
                adressebeskyttelse = null,
                navn = listOf(Navn("Fornavn", "Mellomnavn", "Etternavn")),
                statsborgerskap = null,
                foedsel = null
        )

        every { client.hentPerson(any(), any()) } returns PersonResponse(HentPerson(pdlPerson))

        val resultat = service.hentPerson("12345")!!

        val navn = resultat.navn!!.first()
        assertEquals("Fornavn", navn.fornavn)
        assertEquals("Mellomnavn", navn.mellomnavn)
        assertEquals("Etternavn", navn.etternavn)
    }

    @Test
    fun hentIdenter() {
        val identer = listOf(
                IdentInformasjon("1", IdentGruppe.AKTORID, false),
                IdentInformasjon("2", IdentGruppe.FOLKEREGISTERIDENT, false),
                IdentInformasjon("3", IdentGruppe.NPID, false)
        )

        every { client.hentIdenter(any(), any()) } returns IdenterResponse(HentIdenter(identer))

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

        every { client.hentIdenter(any(), any()) } returns IdenterResponse(HentIdenter(identer))

        val aktorId = service.hentAktorId("12345")

        assertEquals("1", aktorId)
    }

    @Test
    fun hentAktorId_graphqlErrorThrowsException() {
        val errors = listOf(ResponseError(message = "unauthorized"))

        every { client.hentIdenter(any(), any()) } returns IdenterResponse(data = null, errors = errors)

        assertThrows<Exception> {
            service.hentAktorId("12345")
        }
    }
}