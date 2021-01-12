package no.nav.eessi.pensjon.pdl

import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
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
class PdlConfiguration(private val securityTokenExchangeService: STSService) {

    @Bean
    fun pdlRestTemplate(templateBuilder: RestTemplateBuilder): RestTemplate {
        return templateBuilder
                .errorHandler(DefaultResponseErrorHandler())
                .additionalInterceptors(
                        RequestIdHeaderInterceptor(),
                        PdlTokenInterceptor(securityTokenExchangeService))
                .build().apply {
                    requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
                }
    }
}

// TODO: Støtte bruker- OG system-token
class PdlTokenInterceptor(private val securityTokenExchangeService: STSService) : ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest,
                           body: ByteArray,
                           execution: ClientHttpRequestExecution): ClientHttpResponse {

        val token = securityTokenExchangeService.getSystemOidcToken()

        request.headers[HttpHeaders.CONTENT_TYPE] = "application/json"
        request.headers[HttpHeaders.AUTHORIZATION] = "Bearer $token"
        request.headers["Nav-Consumer-Token"] = "Bearer $token"
        request.headers["Tema"] = "PEN"

        return execution.execute(request, body)
    }
}

