package no.nav.eessi.pensjon.personoppslag.pdl

import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import no.nav.eessi.pensjon.shared.retry.IOExceptionRetryInterceptor
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
class PdlConfiguration {

    @Bean
    fun pdlRestTemplate(templateBuilder: RestTemplateBuilder, pdlTokenComponent: PdlTokenCallBack): RestTemplate {

        return templateBuilder
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors(
                RequestIdHeaderInterceptor(),
                IOExceptionRetryInterceptor(),
                RequestResponseLoggerInterceptor(),
                PdlInterceptor(pdlTokenComponent))
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
            logger.debug("tokenIntercetorRequest: accessToken: ${token.accessToken}")

            request.headers[HttpHeaders.CONTENT_TYPE] = "application/json"
            request.headers["Tema"] = "PEN"

            // [Borger, Saksbehandler eller System]
            request.headers.setBearerAuth(token.accessToken)
            return execution.execute(request, body)
        }
   }
}

@Component
@Lazy
@Order(Ordered.LOWEST_PRECEDENCE)
class PdlTokenComponent(private val tokenValidationContextHolder: TokenValidationContextHolder): PdlTokenCallBack {

    override fun callBack(): PdlToken {
        return PdlTokenImp(accessToken = "token")
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