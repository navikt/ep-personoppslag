package no.nav.eessi.pensjon.personoppslag.pdl

import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import no.nav.eessi.pensjon.security.sts.STSService
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


@Configuration
open class PdlConfiguration(private val securityTokenExchangeService: STSService) {

    open fun userToken(): String? = null

    @Bean
    fun pdlRestTemplate(templateBuilder: RestTemplateBuilder): RestTemplate {
        return templateBuilder
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors(
                RequestIdHeaderInterceptor(),
                RequestResponseLoggerInterceptor(),
                PdlTokenInterceptor(userToken(), securityTokenExchangeService))
            .build().apply {
                requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
            }
    }

}

class PdlTokenInterceptor(private val userTokan: String? = null, private val securityTokenExchangeService: STSService) : ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest,
                           body: ByteArray,
                           execution: ClientHttpRequestExecution): ClientHttpResponse {

        return if (userTokan == null) {
            execution.execute(PdlSystemTokenInterceptor(securityTokenExchangeService).intercept(request), body)
        } else {
            execution.execute(PdlUserTokenInterceptor(userTokan, securityTokenExchangeService).intercept(request), body)
        }
    }
}

//system-token
internal class PdlSystemTokenInterceptor(private val securityTokenExchangeService: STSService) {

    fun intercept(request: HttpRequest): HttpRequest {

        val token = securityTokenExchangeService.getSystemOidcToken()

        request.headers[HttpHeaders.CONTENT_TYPE] = "application/json"
        request.headers["Tema"] = "PEN"

        // [Borger, Saksbehandler eller System]
        request.headers[HttpHeaders.AUTHORIZATION] = "Bearer $token"

        // [System]
        request.headers["Nav-Consumer-Token"] = "Bearer $token"

        return request
    }
}

//bruker-token
internal class PdlUserTokenInterceptor(private val userTokan: String, private val securityTokenExchangeService: STSService) {

    fun intercept(request: HttpRequest): HttpRequest {

        val token = securityTokenExchangeService.getSystemOidcToken()

        request.headers[HttpHeaders.CONTENT_TYPE] = "application/json"
        request.headers["Tema"] = "PEN"

        // [Borger, Saksbehandler eller System]
        request.headers[HttpHeaders.AUTHORIZATION] = "Bearer $userTokan"

        // [System]
        request.headers["Nav-Consumer-Token"] = "Bearer $token"

        return request
    }
}