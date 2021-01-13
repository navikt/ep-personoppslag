package no.nav.eessi.pensjon.personoppslag.pdl

import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseGradering
import no.nav.eessi.pensjon.personoppslag.pdl.model.AktoerId
import no.nav.eessi.pensjon.personoppslag.pdl.model.GeografiskTilknytning
import no.nav.eessi.pensjon.personoppslag.pdl.model.Ident
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentGruppe
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentInformasjon
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentType
import no.nav.eessi.pensjon.personoppslag.pdl.model.NorskIdent
import no.nav.eessi.pensjon.personoppslag.pdl.model.Npid
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

    /**
     * Funksjon for å hente ut gjeldende [Ident]
     *
     * @param identTypeWanted: Hvilken [IdentType] man vil hente ut.
     * @param ident: Identen til personen man vil hente ut annen ident for.
     *
     * @return [Ident] av valgt [IdentType]
     */
    fun <T : IdentType, R : IdentType> hentIdent(identTypeWanted: R, ident: Ident<T>): Ident<R>? {
        val result = hentIdenter(ident)
                .firstOrNull { it.gruppe == identTypeWanted.gruppe }
                ?.ident ?: throw HttpClientErrorException(HttpStatus.NOT_FOUND)

        @Suppress("USELESS_CAST", "UNCHECKED_CAST")
        return when (identTypeWanted as IdentType) {
            is IdentType.NorskIdent -> NorskIdent(result) as Ident<R>
            is IdentType.AktoerId -> AktoerId(result) as Ident<R>
            is IdentType.Npid -> Npid(result) as Ident<R>
        }
    }

    /**
     * Funksjon for å hente ut alle identer til en person.
     *
     * @param ident: Identen til personen man vil hente ut identer for. Bruk [NorskIdent], [AktoerId], eller [Npid]
     *
     * @return Liste med [IdentInformasjon]
     */
    fun <T : IdentType> hentIdenter(ident: Ident<T>): List<IdentInformasjon> {
        val response = client.hentIdenter(ident.id)

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
