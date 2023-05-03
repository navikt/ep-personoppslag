package no.nav.eessi.pensjon.personoppslag.pdl

import jakarta.annotation.PostConstruct
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.metrics.MetricsHelper.Metric
import no.nav.eessi.pensjon.metrics.MetricsHelper.Toggle.OFF
import no.nav.eessi.pensjon.personoppslag.pdl.model.*
import no.nav.eessi.pensjon.utils.toJson
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDateTime

@Service
class PersonService(
    private val client: PersonClient,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper.ForTest()
) {

    private val logger = LoggerFactory.getLogger(PersonService::class.java)

    private lateinit var hentPersonMetric: Metric
    private lateinit var hentPersonnavnMetric: Metric
    private lateinit var harAdressebeskyttelseMetric: Metric
    private lateinit var hentAktoerIdMetric: Metric
    private lateinit var hentIdentMetric: Metric
    private lateinit var hentIdenterMetric: Metric
    private lateinit var hentGeografiskTilknytningMetric: Metric
    private lateinit var sokPersonMetric: Metric
    private lateinit var hentPersonUidMetric: Metric

    @PostConstruct
    fun initMetrics() {
        hentPersonMetric = metricsHelper.init("hentPerson", alert = OFF)
        hentPersonnavnMetric = metricsHelper.init("hentPersonnavn", alert = OFF)
        harAdressebeskyttelseMetric = metricsHelper.init("harAdressebeskyttelse", alert = OFF)
        hentAktoerIdMetric = metricsHelper.init("hentAktoerId", alert = OFF)
        hentIdentMetric = metricsHelper.init("hentIdent", alert = OFF)
        hentIdenterMetric = metricsHelper.init("hentIdenter", alert = OFF)
        hentGeografiskTilknytningMetric = metricsHelper.init("hentGeografiskTilknytning", alert = OFF)
        sokPersonMetric = metricsHelper.init("sokPersonMetric", alert = OFF)
        hentPersonUidMetric = metricsHelper.init("hentPersonUid", alert = OFF)
    }


    fun <T : IdentType> hentPersonUtenlandskIdent(ident: Ident<T>): PersonUtenlandskIdent? {
        return hentPersonMetric.measure {
            val response = client.hentPersonUtenlandsIdent(ident.id)

            if (!response.errors.isNullOrEmpty())
                handleError(response.errors)

            return@measure response.data?.hentPerson
                ?.let {
                    val identer = hentIdenter(ident)
                    konverterTilPersonMedUid(it, identer)
                }
        }
    }

    internal fun konverterTilPersonMedUid(
        pdlPerson: HentPersonUtenlandskIdent,
        identer: List<IdentInformasjon>,
        ): PersonUtenlandskIdent {

        val navn = pdlPerson.navn
            .maxByOrNull { it.metadata.sisteRegistrertDato() }

        val kjoenn = pdlPerson.kjoenn
            .maxByOrNull { it.metadata.sisteRegistrertDato() }

        return PersonUtenlandskIdent(
            identer,
            navn,
            kjoenn,
            pdlPerson.utenlandskIdentifikasjonsnummer
        )
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

            return@measure response.data?.hentPerson?.let {
                    val identer = hentIdenter(ident)
                    val geografiskTilknytning = hentGeografiskTilknytning(ident)
                    val utenlandskIdentifikasjonsnummer = hentPersonUtenlandskIdent(ident)?.utenlandskIdentifikasjonsnummer ?: emptyList()
                    konverterTilPerson(it, identer, geografiskTilknytning, utenlandskIdentifikasjonsnummer)
                }
        }
    }


    fun hentPersonnavn(norskIdent: NorskIdent): Navn? {
        return hentPersonnavnMetric.measure {
            val response = client.hentPersonnavn(norskIdent.id)

            if (!response.errors.isNullOrEmpty())
                handleError(response.errors)

            return@measure response.data?.hentPerson?.navn?.
                maxByOrNull {
                    if (it.metadata.master == "FREG") it.folkeregistermetadata?.ajourholdstidspunkt?:LocalDateTime.of(1900,1,1,0,0,0)
                    else it.metadata.sisteRegistrertDato()
                }
        }
    }

    internal fun konverterTilPerson(
            pdlPerson: HentPerson,
            identer: List<IdentInformasjon>,
            geografiskTilknytning: GeografiskTilknytning?,
            utenlandskIdentifikasjonsnummer: List<UtenlandskIdentifikasjonsnummer>
        ): Person {

            val navn = pdlPerson.navn
                .maxByOrNull { it.metadata.sisteRegistrertDato() }

            val graderingListe = pdlPerson.adressebeskyttelse
                .map { it.gradering }
                .distinct()

            val statsborgerskap = pdlPerson.statsborgerskap
                .distinctBy { it.land }

            val foedsel = pdlPerson.foedsel
                .maxByOrNull { it.metadata.sisteRegistrertDato() }

            val bostedsadresse = pdlPerson.bostedsadresse.filter { !it.metadata.historisk }
                .maxByOrNull { it.metadata.sisteRegistrertDato() }

            val oppholdsadresse = pdlPerson.oppholdsadresse.filter { !it.metadata.historisk }
                .maxByOrNull { it.metadata.sisteRegistrertDato() }

            val kontaktadresse = pdlPerson.kontaktadresse?.filter { !it.metadata.historisk }
                ?.maxByOrNull { it.metadata.sisteRegistrertDato() }

            val kontaktinformasjonForDoedsbo = pdlPerson.kontaktinformasjonForDoedsbo.filter { !it.metadata.historisk }
                .maxByOrNull { it.metadata.sisteRegistrertDato() }

            val kjoenn = pdlPerson.kjoenn
                .maxByOrNull { it.metadata.sisteRegistrertDato() }

            val doedsfall = pdlPerson.doedsfall.filter { !it.metadata.historisk }
                .filterNot { it.doedsdato == null }
                .maxByOrNull { it.metadata.sisteRegistrertDato() }

            val forelderBarnRelasjon = pdlPerson.forelderBarnRelasjon
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
                forelderBarnRelasjon,
                sivilstand,
                kontaktadresse,
                kontaktinformasjonForDoedsbo,
                utenlandskIdentifikasjonsnummer
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
                    .filterNot { it.person == null }
                    .flatMap { it.person!!.adressebeskyttelse }
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

    fun sokPerson(sokKriterier: SokKriterier): Set<IdentInformasjon> {
        return sokPersonMetric.measure {
            val response = client.sokPerson(makeListCriteriaFromSok(sokKriterier))

            if (!response.errors.isNullOrEmpty())
                handleError(response.errors)

            val hits = response.data?.sokPerson?.hits

            return@measure if (hits?.size == 1) {
                hits.first().identer.toSet()
            } else {
                emptySet()
            }
        }
    }

    private fun makeListCriteriaFromSok(sokKriterier: SokKriterier): List<SokCriteria> {
        val INNEHOLDER = "contains"
        val ER_LIK = "equals"

        return listOf(
            SokCriteria("person.navn.fornavn",mapOf(INNEHOLDER to sokKriterier.fornavn)),
            SokCriteria("person.navn.etternavn", mapOf(INNEHOLDER to sokKriterier.etternavn)),
            SokCriteria("person.foedsel.foedselsdato", mapOf(ER_LIK to "${sokKriterier.foedselsdato}"))
        )

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
        errors.forEach{logger.error("PDL error: \n ${it.toJson()}")}

        val error = errors.first()

        val code = error.extensions?.code ?: "unknown_error"
        val message = error.message ?: "Error message from PDL is missing"

        throw PersonoppslagException(message, code)
    }

}

class PersonoppslagException(message: String, val code: String) : RuntimeException("$code: $message") {
    @Deprecated("Bruk PersonoppslagException(message, code)")
    constructor(combinedMessage: String): this(combinedMessage.split(": ").first(), combinedMessage.split(": ")[1])
}
