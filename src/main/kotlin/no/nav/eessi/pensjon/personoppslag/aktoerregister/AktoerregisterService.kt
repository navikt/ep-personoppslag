package no.nav.eessi.pensjon.personoppslag.aktoerregister

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import javax.annotation.PostConstruct


sealed class IdentGruppe(val text: String) {
    override fun toString() = text
    object AktoerId : IdentGruppe("AktoerId")
    object NorskIdent : IdentGruppe("NorskIdent")
}

sealed class Ident<T: IdentGruppe> {
    abstract val id: String
}
data class AktoerId(override val id: String) : Ident<IdentGruppe.AktoerId>()
data class NorskIdent(override val id: String) : Ident<IdentGruppe.NorskIdent>()

@ConditionalOnBean(name=["aktoerregisterRestTemplate"])
@Service
@Deprecated("TPS skal fases ut til fordel for PDL. Bruk derfor PDL PersonService i stedet.")
class AktoerregisterService(private val aktoerregisterRestTemplate: RestTemplate,
                            @Value("\${NAIS_APP_NAME}") private val appName: String,
                            @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    private val logger = LoggerFactory.getLogger(AktoerregisterService::class.java)

    private lateinit var AktoerregisterOppslag: MetricsHelper.Metric
    private lateinit var AktoerregisterRequest: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        AktoerregisterOppslag = metricsHelper.init("AktoerregisterOppslag")
        AktoerregisterRequest = metricsHelper.init("AktoerregisterRequest", alert = MetricsHelper.Toggle.OFF)
    }

    @Throws(AktoerregisterException::class)
    fun <T: IdentGruppe, R: IdentGruppe> hentGjeldendeIdent(identGruppeWanted: R, ident: Ident<T>): Ident<R>? =
            this.AktoerregisterOppslag.measure {
                val response = try {
                    doRequest(ident.id, identGruppeWanted.text)
                } catch (ex: Exception) {
                    throw AktoerregisterException("Problem looking up ${identGruppeWanted} for $ident: ${ex.message}", ex)
                }
                val identInfo = response[ident.id]
                if (identInfo == null) {
                    logger.info("Ingen IdentInfo fra Aktoerregisteret funnet for $ident")
                    return@measure null
                }
                if (identInfo.feilmelding != null) {
                    throw AktoerregisterException(identInfo.feilmelding)
                }
                if (identInfo.identer.isNullOrEmpty()) {
                    logger.info("Tom liste over Identer fra Aktoerregisteret for $ident")
                    return@measure null
                }
                if (identInfo.identer.size > 1) {
                    throw AktoerregisterException("Forventet 1 gjeldende ${identGruppeWanted}, fant ${identInfo.identer.size}")
                }
                val result = when (identGruppeWanted as IdentGruppe) {
                    is IdentGruppe.NorskIdent -> NorskIdent(response[ident.id]?.identer!![0].ident) as Ident<R>
                    is IdentGruppe.AktoerId -> AktoerId(response[ident.id]?.identer!![0].ident) as Ident<R>
                }
                result
            }

    private data class Identinfo(
            val ident: String,
            val identgruppe: String,
            val gjeldende: Boolean
    )

    private data class IdentinfoForAktoer(
            val identer: List<Identinfo>?,
            val feilmelding: String?
    )

    private fun doRequest(ident: String,
                          identGruppe: String,
                          gjeldende: Boolean = true): Map<String, IdentinfoForAktoer> {
        return AktoerregisterRequest.measure {
            val headers = HttpHeaders()
            headers["Nav-Personidenter"] = ident
            headers["Nav-Consumer-Id"] = appName
            headers["Nav-Call-Id"] = UUID.randomUUID().toString()
            val requestEntity = HttpEntity<String>(headers)

            val uriBuilder = UriComponentsBuilder.fromPath("/identer")
                    .queryParam("identgruppe", identGruppe)
                    .queryParam("gjeldende", gjeldende)
            logger.info("Kaller aktørregisteret: /identer")

            val responseEntity =
                    aktoerregisterRestTemplate.exchange(uriBuilder.toUriString(),
                            HttpMethod.GET,
                            requestEntity,
                            String::class.java)

            jacksonObjectMapper().readValue(responseEntity.body!!)
        }
    }
}

@Deprecated("This should not be used by clients")
@ResponseStatus(value = HttpStatus.NOT_FOUND)
class AktoerregisterIkkeFunnetException(message: String?) : Exception(message)

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class AktoerregisterException(message: String, cause: Throwable? = null) : Exception(message, cause)

@Deprecated("This should not be used by clients")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class ManglerAktoerIdException(message: String) : Exception(message)
