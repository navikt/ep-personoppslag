package no.nav.eessi.pensjon.personoppslag.pdl

import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
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
import java.util.*

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
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

    internal class PdlInterceptor(private val pdlToken: PdlTokenCallBack): ClientHttpRequestInterceptor {

        private val logger = LoggerFactory.getLogger(PdlInterceptor::class.java)

        override fun intercept(request: HttpRequest,
                               body: ByteArray,
                               execution: ClientHttpRequestExecution): ClientHttpResponse {


            val token = pdlToken.callBack()
            logger.debug("tokenIntercetorRequest: userToken: ${token.isUserToken}")
            logger.debug("""
                oAuth-token:
                  system: ${token.systemToken},
                  user: ${token.userToken}
                """)

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
open class PdlTokenComponent(private val clientConfigurationProperties: ClientConfigurationProperties, private val oAuth2AccessTokenService: OAuth2AccessTokenService): PdlTokenCallBack {

    override fun callBack(): PdlToken {
        return PdlSystemOidcToken(clientConfigurationProperties, oAuth2AccessTokenService).callBack()
    }

}

internal class PdlSystemOidcToken(private val clientConfigurationProperties: ClientConfigurationProperties, private val oAuth2AccessTokenService: OAuth2AccessTokenService): PdlTokenCallBack {
    override fun callBack(): PdlToken {
        val clientProperties =  Optional.ofNullable(clientConfigurationProperties.registration["pdl-credentials"]).orElseThrow { RuntimeException("could not find oauth2 client config for pdl-credentials") }
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        val token = response.accessToken
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