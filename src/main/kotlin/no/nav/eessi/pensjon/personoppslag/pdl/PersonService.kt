package no.nav.eessi.pensjon.personoppslag.pdl

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.metrics.MetricsHelper.Metric
import no.nav.eessi.pensjon.metrics.MetricsHelper.Toggle.OFF
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseGradering
import no.nav.eessi.pensjon.personoppslag.pdl.model.AktoerId
import no.nav.eessi.pensjon.personoppslag.pdl.model.GeografiskTilknytning
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentPerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.Ident
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentGruppe
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentInformasjon
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentType
import no.nav.eessi.pensjon.personoppslag.pdl.model.NorskIdent
import no.nav.eessi.pensjon.personoppslag.pdl.model.Npid
import no.nav.eessi.pensjon.personoppslag.pdl.model.Person
import no.nav.eessi.pensjon.personoppslag.pdl.model.ResponseError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.lang.RuntimeException
import javax.annotation.PostConstruct

@Service
class PersonService(
    private val client: PersonClient,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())
) {

    private lateinit var hentPersonMetric: Metric
    private lateinit var harAdressebeskyttelseMetric: Metric
    private lateinit var hentAktoerIdMetric: Metric
    private lateinit var hentIdentMetric: Metric
    private lateinit var hentIdenterMetric: Metric
    private lateinit var hentGeografiskTilknytningMetric: Metric

    @PostConstruct
    fun initMetrics() {
        hentPersonMetric = metricsHelper.init("hentPerson", alert = OFF)
        harAdressebeskyttelseMetric = metricsHelper.init("harAdressebeskyttelse", alert = OFF)
        hentAktoerIdMetric = metricsHelper.init("hentAktoerId", alert = OFF)
        hentIdentMetric = metricsHelper.init("hentIdent", alert = OFF)
        hentIdenterMetric = metricsHelper.init("hentIdenter", alert = OFF)
        hentGeografiskTilknytningMetric = metricsHelper.init("hentGeografiskTilknytning", alert = OFF)
    }

    /**
     * Funksjon for å hente ut person basert på fnr.
     *
     * @param ident: Identen til personen man vil hente ut identer for. Bruk [NorskIdent], [AktoerId], eller [Npid]
     *
     * @return [Person]
     */
    fun <T : IdentType> hentPerson(ident: Ident<T>): Person? {
        return hentPersonMetric.measure {
            val response = client.hentPerson(ident.id)

            if (!response.errors.isNullOrEmpty())
                handleError(response.errors)

            return@measure response.data?.hentPerson
                ?.let { konverterTilPerson(ident, it) }
        }
    }

    private fun <T : IdentType> konverterTilPerson(ident: Ident<T>, pdlPerson: HentPerson): Person {
        val identer = hentIdenter(ident)
        val geografiskTilknytning = hentGeografiskTilknytning(ident)

        val navn = pdlPerson.navn
                .singleOrNull()

        val graderingListe = pdlPerson.adressebeskyttelse
                .map { it.gradering }
                .distinct()

        val statsborgerskap = pdlPerson.statsborgerskap
                .distinctBy { it.land }

        val foedsel = pdlPerson.foedsel
                .filterNot { it.folkeregistermetadata?.gyldighetstidspunkt == null }
                .maxBy { it.folkeregistermetadata!!.gyldighetstidspunkt!! }

        val bostedsadresse = pdlPerson.bostedsadresse
                .filterNot { it.gyldigFraOgMed == null }
                .maxBy { it.gyldigFraOgMed!! }

        val oppholdsadresse = pdlPerson.oppholdsadresse
                .filterNot { it.gyldigFraOgMed == null }
                .maxBy { it.gyldigFraOgMed!! }

        val kjoenn = pdlPerson.kjoenn
                .filterNot { it.folkeregistermetadata?.gyldighetstidspunkt == null }
                .maxBy { it.folkeregistermetadata!!.gyldighetstidspunkt!! }

        val doedsfall = pdlPerson.doedsfall
                .filterNot { it.doedsdato == null }
                .filterNot { it.folkeregistermetadata?.gyldighetstidspunkt == null }
                .maxBy { it.folkeregistermetadata!!.gyldighetstidspunkt!! }

        val familierelasjoner = pdlPerson.familierelasjoner
        val sivilstand = pdlPerson.sivilstand

        return Person(
                identer,
                navn,
                graderingListe,
                bostedsadresse,
                oppholdsadresse,
                statsborgerskap,
                foedsel,
                geografiskTilknytning,
                kjoenn,
                doedsfall,
                familierelasjoner,
                sivilstand
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
        if (fnr.isEmpty() || gradering.isEmpty()) return false

        return harAdressebeskyttelseMetric.measure {
            val response = client.hentAdressebeskyttelse(fnr)

            if (!response.errors.isNullOrEmpty())
                handleError(response.errors)

            val personer = response.data?.hentPersonBolk
                ?: return@measure false

            return@measure personer
                .flatMap { it.person.adressebeskyttelse }
                .any { it.gradering in gradering }
        }
    }

    /**
     * Funksjon for å hente ut en person sin Aktør ID.
     *
     * @param fnr: Fødselsnummeret til personen man vil hente ut Aktør ID for.
     *
     * @return [IdentInformasjon] med Aktør ID, hvis funnet
     */
    fun hentAktorId(fnr: String): AktoerId {
        return hentAktoerIdMetric.measure {
            val response = client.hentAktorId(fnr)

            if (!response.errors.isNullOrEmpty())
                handleError(response.errors)

            return@measure response.data?.hentIdenter?.identer
                ?.firstOrNull { it.gruppe == IdentGruppe.AKTORID }
                ?.let { AktoerId(it.ident) }
                ?: throw HttpClientErrorException(HttpStatus.NOT_FOUND)
        }
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
        return hentIdentMetric.measure {
            val result = hentIdenter(ident)
                .firstOrNull { it.gruppe == identTypeWanted.gruppe }
                ?.ident ?: throw HttpClientErrorException(HttpStatus.NOT_FOUND)

            @Suppress("USELESS_CAST", "UNCHECKED_CAST")
            return@measure when (identTypeWanted as IdentType) {
                is IdentType.NorskIdent -> NorskIdent(result) as Ident<R>
                is IdentType.AktoerId -> AktoerId(result) as Ident<R>
                is IdentType.Npid -> Npid(result) as Ident<R>
            }
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
        return hentIdenterMetric.measure {
            val response = client.hentIdenter(ident.id)

            if (!response.errors.isNullOrEmpty())
                handleError(response.errors)

            return@measure response.data?.hentIdenter?.identer ?: emptyList()
        }
    }

    /**
     * Funksjon for å hente ut en person sin geografiske tilknytning.
     *
     * @param ident: Identen til personen man vil hente ut identer for. Bruk [NorskIdent], [AktoerId], eller [Npid]
     *
     * @return [GeografiskTilknytning]
     */
    fun <T : IdentType> hentGeografiskTilknytning(ident: Ident<T>): GeografiskTilknytning? {
        return hentGeografiskTilknytningMetric.measure {
            val response = client.hentGeografiskTilknytning(ident.id)

            if (!response.errors.isNullOrEmpty())
                handleError(response.errors)

            return@measure response.data?.geografiskTilknytning
        }
    }

    private fun handleError(errors: List<ResponseError>) {
        val error = errors.first()

        val code = error.extensions?.code ?: "unknown_error"
        val message = error.message ?: "Error message from PDL is missing"

        throw PersonoppslagException("$code: $message")
    }

}

class PersonoppslagException(message: String) : RuntimeException(message)
