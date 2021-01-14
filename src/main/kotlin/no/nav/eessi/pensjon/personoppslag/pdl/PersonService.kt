package no.nav.eessi.pensjon.personoppslag.pdl

import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseGradering
import no.nav.eessi.pensjon.personoppslag.pdl.model.AktoerId
import no.nav.eessi.pensjon.personoppslag.pdl.model.Bostedsadresse
import no.nav.eessi.pensjon.personoppslag.pdl.model.Foedsel
import no.nav.eessi.pensjon.personoppslag.pdl.model.Folkeregistermetadata
import no.nav.eessi.pensjon.personoppslag.pdl.model.GeografiskTilknytning
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentPerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.Ident
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentGruppe
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentInformasjon
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentType
import no.nav.eessi.pensjon.personoppslag.pdl.model.NorskIdent
import no.nav.eessi.pensjon.personoppslag.pdl.model.Npid
import no.nav.eessi.pensjon.personoppslag.pdl.model.Person
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException

@Service
class PersonService(private val client: PersonClient) {

    /**
     * Funksjon for å hente ut person basert på fnr.
     *
     * @param ident: Identen til personen man vil hente ut identer for. Bruk [NorskIdent], [AktoerId], eller [Npid]
     *
     * @return [Person]
     */
    fun <T : IdentType> hentPerson(ident: Ident<T>): Person? {
        val response = client.hentPerson(ident.id)

        if (!response.errors.isNullOrEmpty()) {
            val errorMsg = response.errors.joinToString { it.message ?: "" }
            throw Exception(errorMsg)
        }

        return response.data?.hentPerson
                ?.let { konverterTilPerson(ident, it) }
    }

    private fun <T : IdentType> konverterTilPerson(ident: Ident<T>, pdlPerson: HentPerson): Person {
        val identer = hentIdenter(ident)

        val navn = pdlPerson.navn.singleOrNull()

        val graderingListe = pdlPerson.adressebeskyttelse
                .map { it.gradering }
                .distinct()

        val statsborgerskap = pdlPerson.statsborgerskap
                .distinctBy { it.land }

        val foedsel = pdlPerson.foedsel
                .filterNot { it.folkeregistermetadata == null }
                .filterNot { it.folkeregistermetadata?.gyldighetstidspunkt == null }
                .maxBy { it.folkeregistermetadata!!.gyldighetstidspunkt!! }

        val bostedsadresse = pdlPerson.bostedsadresse.maxBy { it.gyldigFraOgMed }
        val oppholdsadresse = pdlPerson.oppholdsadresse.maxBy { it.gyldigFraOgMed }

        return Person(
                identer,
                navn,
                graderingListe,
                bostedsadresse,
                oppholdsadresse,
                statsborgerskap,
                foedsel
        )
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
                .flatMap { it.person.adressebeskyttelse }
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
    fun <T : IdentType, R : IdentType> hentIdent(identTypeWanted: R, ident: Ident<T>): Ident<R> {
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
     * @param ident: Identen til personen man vil hente ut identer for. Bruk [NorskIdent], [AktoerId], eller [Npid]
     *
     * @return [GeografiskTilknytning]
     */
    fun <T : IdentType> hentGeografiskTilknytning(ident: Ident<T>): GeografiskTilknytning? {
        val response = client.hentGeografiskTilknytning(ident.id)

        if (!response.errors.isNullOrEmpty()) {
            val errorMsg = response.errors.joinToString { it.message ?: "" }
            throw Exception(errorMsg)
        }

        return response.data?.geografiskTilknytning
    }
}
