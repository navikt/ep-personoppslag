package no.nav.eessi.pensjon.personoppslag.personv3

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.security.sts.STSClientConfig
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Informasjonsbehov
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import javax.annotation.PostConstruct
import javax.xml.ws.WebServiceException
import javax.xml.ws.soap.SOAPFaultException

/**
 * @param metricsHelper Usually injected by Spring Boot, can be set manually in tests - no way to read metrics if not set.
 */
@Service
class PersonV3Service(
        private val service: PersonV3,
        private val stsClientConfig: STSClientConfig,
        @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())
) {

    private val logger: Logger by lazy { LoggerFactory.getLogger(PersonV3Service::class.java) }

    private lateinit var hentPerson: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        hentPerson = metricsHelper.init("hentperson", alert = MetricsHelper.Toggle.OFF)
    }

    @Throws(PersonV3IkkeFunnetException::class, PersonV3SikkerhetsbegrensningException::class, UgyldigIdentException::class)
    @Retryable(include = [SOAPFaultException::class])
    fun hentPersonResponse(fnr: String): HentPersonResponse {
        logger.info("Henter person fra PersonV3Service")
        stsClientConfig.configureRequestSamlToken(service)

        val request = HentPersonRequest().apply {
            withAktoer(PersonIdent().withIdent(
                    NorskIdent().withIdent(fnr)))

            withInformasjonsbehov(listOf(
                    Informasjonsbehov.ADRESSE,
                    Informasjonsbehov.FAMILIERELASJONER
            ))
        }

        return hentPerson.measure {
            return@measure try {
                logger.info("Kaller PersonV3.hentPerson service")
                service.hentPerson(request)
            } catch (personIkkefunnet: HentPersonPersonIkkeFunnet) {
                logger.error("Kaller PersonV3.hentPerson service Feilet: $personIkkefunnet")
                throw PersonV3IkkeFunnetException(personIkkefunnet.message!!)
            } catch (personSikkerhetsbegrensning: HentPersonSikkerhetsbegrensning) {
                logger.error("Kaller PersonV3.hentPerson service Feilet $personSikkerhetsbegrensning")
                throw PersonV3SikkerhetsbegrensningException(personSikkerhetsbegrensning.message!!)
            } catch (soapFaultException: SOAPFaultException) {
                if (soapFaultException.message != null && soapFaultException.message!!.contains("S610006F")) { //https://confluence.adeo.no/x/rYJ4Bw
                    logger.warn("TPS rapporterer S610006F, trolig fodelsdato postfixet med nuller: '${maskerFnr(fnr)}' - $soapFaultException")
                    throw UgyldigIdentException("TPS rapporterer S610006F, trolig fodelsdato postfixet med nuller", soapFaultException)
                } else {
                    logger.error("Ukjent feil i PersonV3: fnr: ${maskerFnr(fnr)}, ${soapFaultException.message}", soapFaultException)
                    throw soapFaultException
                }
            } catch (ex: Exception) {
                logger.error("Ukjent feil i PersonV3: fnr: ${maskerFnr(fnr)}, ${ex.message}", ex)
                throw ex
            }
        }
    }

    fun maskerFnr(fnr: String): String{
        if(fnr.length == 11 && !fnr.contains("00000") ){
            return fnr.replaceRange(6, 11, "*****")
        }
        return fnr
    }

    fun hentBruker(fnr: String): Bruker? {

        return try {
            val response = hentPersonResponse(fnr)

            logger.debug(jacksonObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(response.person))

            response.person as Bruker
        } catch (ex: Exception) {
            logger.warn("Feil ved henting av Bruker fra TPS, sjekk ident? ($fnr)", ex)
            null
        }

    }


    @Retryable(include = [SOAPFaultException::class, WebServiceException::class])
    fun hentPerson(fnr: String): Bruker? {
        return hentPerson.measure {
            logger.info("Henter person fra PersonV3Service")

            try {
                logger.info("Kaller PersonV3.hentPerson service")
                val resp = kallPersonV3(fnr)
                resp.person as Bruker
            } catch (pif: HentPersonPersonIkkeFunnet) {
                logger.warn("PersonV3: Kunne ikke hente person, ikke funnet", pif)
                null
            } catch (sfe: SOAPFaultException) {
                if (sfe.fault.faultString.contains("F002001F")) {
                    logger.warn("PersonV3: Kunne ikke hente person, ugyldig input", sfe)
                    null
                } else if (sfe.message != null && sfe.message!!.contains("S610006F")) { //https://confluence.adeo.no/x/rYJ4Bw
                    logger.warn("TPS rapporterer S610006F, trolig fodelsdato postfixet med nuller: '${maskerFnr(fnr)}' - $sfe")
                    null
                } else {
                    logger.error("PersonV3: Ukjent SoapFaultException", sfe)
                    throw sfe
                }
            } catch (sb: HentPersonSikkerhetsbegrensning) {
                logger.error("PersonV3: Kunne ikke hente person, sikkerhetsbegrensning", sb)
                throw PersonV3SikkerhetsbegrensningException(sb.message)
            } catch (ex: Exception) {
                logger.error("PersonV3: Kunne ikke hente person", ex)
                throw ex
            }
        }
    }

    private fun kallPersonV3(fnr: String?) : HentPersonResponse{
        val request = HentPersonRequest().apply {
            withAktoer(PersonIdent().withIdent(
                    NorskIdent().withIdent(fnr)))

            withInformasjonsbehov(listOf(
                    Informasjonsbehov.ADRESSE))
        }
        konfigurerSamlToken()
        return  service.hentPerson(request)
    }

    fun konfigurerSamlToken(){
        stsClientConfig.configureRequestSamlToken(service)
    }
}

@ResponseStatus(value = HttpStatus.FORBIDDEN)
class PersonV3SikkerhetsbegrensningException(message: String?): Exception(message)


@ResponseStatus(value = HttpStatus.NOT_FOUND)
class PersonV3IkkeFunnetException(message: String) : RuntimeException(message)

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class UgyldigIdentException(message: String, cause: Throwable? = null) : Exception(message, cause)
