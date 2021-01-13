package no.nav.eessi.pensjon.personoppslag.pdl

import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseGradering
import no.nav.eessi.pensjon.personoppslag.pdl.model.AktoerId
import no.nav.eessi.pensjon.personoppslag.pdl.model.Ident
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentGruppe
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentGruppen
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentInformasjon
import no.nav.eessi.pensjon.personoppslag.pdl.model.NorskIdent
import no.nav.eessi.pensjon.personoppslag.pdl.model.PdlPerson
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException

@Service
class PersonService(private val client: PersonClient) {

    /**
     * Funksjon for å hente ut person basert på fnr.
     *
     * @param fnr: Fødselsnummeret til personen man vil hente ut [GeografiskTilknytning] for.
     *
     * @return [PdlPerson]
     */
    fun hentPerson(fnr: String): PdlPerson? {
        val response = client.hentPerson(fnr)

        if (!response.errors.isNullOrEmpty()) {
            val errorMsg = response.errors.joinToString { it.message ?: "" }
            throw Exception(errorMsg)
        }

        return response.data?.pdlPerson
    }

    /**
     * Funksjon for å sjekke om en liste med personer (fnr.) har en bestemt grad av adressebeskyttelse.
     *
     * @param fnr: Fødselsnummerene til personene man vil sjekke.
     * @param gradering: Graderingen man vil sjekke om personene har.
     *
     * @return [Boolean] "true" dersom en av personene har valgt gradering.
     */
    fun harAdressebeskyttelse(fnr: List<String>, gradering: List<AdressebeskyttelseGradering>): Boolean {
        val response = client.hentAdressebeskyttelse(fnr)

        if (!response.errors.isNullOrEmpty()) {
            val errorMsg = response.errors.joinToString { it.message ?: "" }
            throw Exception(errorMsg)
        }

        val personer = response.data?.hentPersonBolk ?: return false

        return personer
                .mapNotNull { it.person.adressebeskyttelse }
                .flatten()
                .any { it.gradering in gradering }
    }

    /**
     * Funksjon for å hente ut en person sin Aktør ID.
     *
     * @param fnr: Fødselsnummeret til personen man vil hente ut Aktør ID for.
     *
     * @return [IdentInformasjon] med Aktør ID, hvis funnet
     */
    fun hentAktorId(fnr: String): String {
        val response = client.hentAktorId(fnr)

        if (!response.errors.isNullOrEmpty()) {
            val errorMsg = response.errors.joinToString { it.message ?: "" }
            throw Exception(errorMsg)
        }

        return response.data?.hentIdenter?.identer
                ?.firstOrNull { it.gruppe == IdentGruppe.AKTORID }
                ?.ident
                ?: throw HttpClientErrorException(HttpStatus.NOT_FOUND)
    }

    fun <T: IdentGruppen, R: IdentGruppen> hentGjeldendeIdent(identGruppeWanted: R, ident: Ident<T>): Ident<R>? {
        val response = client.hentAktorId(ident.id)

        if (!response.errors.isNullOrEmpty()) {
            val errorMsg = response.errors.joinToString { it.message ?: "" }
            throw Exception(errorMsg)
        }

        val result = when (identGruppeWanted as IdentGruppen) {
            is IdentGruppen.NorskIdent -> {
                val norskident = response.data?.hentIdenter?.identer
                    ?.firstOrNull { it.gruppe == IdentGruppe.FOLKEREGISTERIDENT }
                    ?.ident ?: throw HttpClientErrorException(HttpStatus.NOT_FOUND)
                NorskIdent( norskident ) as Ident<R> }
            is IdentGruppen.AktoerId -> {
                val aktoer = response.data?.hentIdenter?.identer
                    ?.firstOrNull { it.gruppe == IdentGruppe.AKTORID }
                    ?.ident ?: throw HttpClientErrorException(HttpStatus.NOT_FOUND)
                AktoerId( aktoer ) as Ident<R> }
        }
        return result
    }

    /**
     * Funksjon for å hente ut alle identer til en person.
     *
     * @param fnr: Fødselsnummeret til personen man vil hente ut identer for.
     *
     * @return Liste med [IdentInformasjon]
     */
    fun hentIdenter(fnr: String): List<IdentInformasjon> {
        val response = client.hentIdenter(fnr)

        if (!response.errors.isNullOrEmpty()) {
            val errorMsg = response.errors.joinToString { it.message ?: "" }
            throw Exception(errorMsg)
        }

        return response.data?.hentIdenter?.identer ?: emptyList()
    }

    /**
     * Funksjon for å hente ut en person sin geografiske tilknytning.
     *
     * @param fnr: Fødselsnummeret til personen man vil hente ut [GeografiskTilknytning] for.
     *
     * @return [GeografiskTilknytning]
     */
    fun hentGeografiskTilknytning(fnr: String): GeografiskTilknytning? {
        val response = client.hentGeografiskTilknytning(fnr)

        if (!response.errors.isNullOrEmpty()) {
            val errorMsg = response.errors.joinToString { it.message ?: "" }
            throw Exception(errorMsg)
        }

        return response.data?.geografiskTilknytning
    }
}
