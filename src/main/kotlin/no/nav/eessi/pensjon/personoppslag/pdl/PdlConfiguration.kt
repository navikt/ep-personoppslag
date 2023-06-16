package no.nav.eessi.pensjon.personoppslag.pdl

import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.metrics.RequestCountInterceptor
import no.nav.eessi.pensjon.shared.retry.IOExceptionRetryInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
open class PdlConfiguration(@Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper.ForTest()) {

    fun pdlRestTemplate(
        pdlTokenCallBack: PdlTokenCallBack): RestTemplate {

        return RestTemplateBuilder()
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors(
                RequestIdHeaderInterceptor(),
                IOExceptionRetryInterceptor(),
                RequestCountInterceptor(meterRegistry = metricsHelper.registry),
                RequestResponseLoggerInterceptor(),
                PdlInterceptor(pdlTokenCallBack))
            .build().apply {
                requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
            }
    }

    internal class PdlInterceptor(private val pdlToken: PdlTokenCallBack): ClientHttpRequestInterceptor {

        private val logger = LoggerFactory.getLogger(PdlInterceptor::class.java)

        override fun intercept(request: HttpRequest,
                               body: ByteArray,
                               execution: ClientHttpRequestExecution): ClientHttpResponse {

            val token = pdlToken.callBack()
            request.headers[HttpHeaders.CONTENT_TYPE] = "application/json"
            request.headers["Tema"] = "PEN"

            // [Borger, Saksbehandler eller System]
            request.headers.setBearerAuth(token.accessToken)
            return execution.execute(request, body)
        }
   }
}

interface PdlTokenCallBack {
    fun callBack(): PdlToken
}

interface PdlToken{
    val accessToken: String
}

class PdlTokenImp(
    override val accessToken: String,
) : PdlToken