package no.nav.eessi.pensjon.personoppslag.pdl

import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import no.nav.eessi.pensjon.security.sts.STSService
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

@Configuration
class PdlConfiguration {

    @Bean
    fun pdlRestTemplate(templateBuilder: RestTemplateBuilder, pdlTokenComponent: PdlTokenCallBack): RestTemplate {
        return templateBuilder
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors(
                RequestIdHeaderInterceptor(),
                RequestResponseLoggerInterceptor(),
                PdlInterceptor(pdlTokenComponent))
            .build().apply {
                requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
            }
    }

    internal class PdlInterceptor(private val pdlTokens: PdlTokenCallBack) : ClientHttpRequestInterceptor {

        private val logger = LoggerFactory.getLogger(PdlInterceptor::class.java)

        override fun intercept(request: HttpRequest,
                               body: ByteArray,
                               execution: ClientHttpRequestExecution): ClientHttpResponse {

            val token = pdlTokens.callBack()

            logger.debug("tokenIntercetorRequest: userToken: ${token.isUserToken}")

            request.headers[HttpHeaders.CONTENT_TYPE] = "application/json"
            request.headers["Tema"] = "PEN"

            // [Borger, Saksbehandler eller System]
            request.headers[HttpHeaders.AUTHORIZATION] = "Bearer ${token.userToken}"

            // [System]
            request.headers["Nav-Consumer-Token"] = "Bearer ${token.systemToken}"

            return execution.execute(request, body)
        }
   }
}

@Component
@Lazy
@Order(Ordered.LOWEST_PRECEDENCE)
open class PdlTokenComponent(private val securityTokenExchangeService: STSService): PdlTokenCallBack {

    override fun callBack(): PdlToken {
        return PdlSystemOidcToken(securityTokenExchangeService).callBack()
    }


}

internal class PdlSystemOidcToken(private val securityTokenExchangeService: STSService): PdlTokenCallBack {
    override fun callBack(): PdlToken {
        val token = securityTokenExchangeService.getSystemOidcToken()
        return PdlTokenImp(systemToken = token, userToken = token, isUserToken = false)
    }
}


interface PdlTokenCallBack {
    fun callBack(): PdlToken
}

interface PdlToken{
    val systemToken: String
    val userToken: String
    val isUserToken: Boolean
}

class PdlTokenImp(
    override val systemToken: String,
    override val userToken: String,
    override val isUserToken: Boolean
) : PdlToken