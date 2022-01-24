package no.nav.eessi.pensjon.personoppslag.pdl

import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.util.*

@Configuration
class PdlConfiguration {

    private val logger = LoggerFactory.getLogger(PdlConfiguration::class.java)

    @Bean
    fun oAuth(clientConfigurationProperties: ClientConfigurationProperties, oAuth2AccessTokenService: OAuth2AccessTokenService): String {
        //val token = pdlTokens.callBack()
        val clientProperties =  Optional.ofNullable(clientConfigurationProperties.registration["begrens-innsyn-credentials"]).orElseThrow { RuntimeException("could not find oauth2 client config for example-onbehalfof") }
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        val token = response.accessToken

        logger.debug("tokenIntercetorRequest: userToken: $token")
        return token
    }

    @Bean
    fun pdlRestTemplate(templateBuilder: RestTemplateBuilder, oatuh: String): RestTemplate {
        return templateBuilder
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors(
                RequestIdHeaderInterceptor(),
                RequestResponseLoggerInterceptor(),
                PdlInterceptor(oatuh))
            .build().apply {
                requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
            }
    }

    internal class PdlInterceptor(private val oatuhtoken: String) : ClientHttpRequestInterceptor {
        override fun intercept(request: HttpRequest,
                               body: ByteArray,
                               execution: ClientHttpRequestExecution): ClientHttpResponse {

            request.headers[HttpHeaders.CONTENT_TYPE] = "application/json"
            request.headers["Tema"] = "PEN"

            // [Borger, Saksbehandler eller System]
            request.headers[HttpHeaders.AUTHORIZATION] = "Bearer $oatuhtoken"

            // [System]
            request.headers["Nav-Consumer-Token"] = "Bearer $oatuhtoken"

            return execution.execute(request, body)
        }
   }
}
