package no.nav.eessi.pensjon.personoppslag.pdl

import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import no.nav.eessi.pensjon.security.sts.STSService
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
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
class PdlConfiguration {

    @Bean
    fun pdlRestTemplate(templateBuilder: RestTemplateBuilder, pdlTokens: List<PdlToken>): RestTemplate {
        return templateBuilder
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors(
                RequestIdHeaderInterceptor(),
                RequestResponseLoggerInterceptor(),
                PdlInterceptor(pdlTokens))
            .build().apply {
                requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
            }
    }

    class PdlInterceptor(private val pdlTokens: List<PdlToken>) : ClientHttpRequestInterceptor {

        private val logger = LoggerFactory.getLogger(PdlInterceptor::class.java)

        override fun intercept(request: HttpRequest,
                               body: ByteArray,
                               execution: ClientHttpRequestExecution): ClientHttpResponse {


            val token = pdlTokens.firstOrNull { it.isUserToken == true } ?: pdlTokens.first { it.isUserToken == false }

            logger.info("tokenIntercetorRequest: userToken: ${token.isUserToken}")

            request.headers[HttpHeaders.CONTENT_TYPE] = "application/json"
            request.headers["Tema"] = "PEN"

            // [Borger, Saksbehandler eller System]
            request.headers[HttpHeaders.AUTHORIZATION] = "Bearer ${token.userToken}"

            // [System]
            request.headers["Nav-Consumer-Token"] = "Bearer ${token.systemToken}"

            return execution.execute(request, body)
        }
    }

    @Bean
    @Lazy
    fun pdlTokenCollection(securityTokenExchangeService: STSService): List<PdlToken> {
        return listOf(pdlSystemOidcToken(securityTokenExchangeService))
    }

    @Bean("pdlSystemToken")
    fun pdlSystemOidcToken(securityTokenExchangeService: STSService): PdlToken {
        val token = securityTokenExchangeService.getSystemOidcToken()
        return PdlTokenImp(systemToken = token, userToken = token, isUserToken = false)
    }

//    @Bean("pdlUserToken")
//    fun pdlUserOidcToken(securityTokenExchangeService: STSService): PdlToken {
//        val token = securityTokenExchangeService.getSystemOidcToken()
//        return PdlTokenImp(systemToken = token, userToken = token, isUserToken = true)
//    }


}

////bruker-token er default imp med null
//open class PdlUserTokenInterceptor(private val securityTokenExchangeService: STSService): PdlToken {
//
//    override fun intercept(request: HttpRequest): HttpRequest? {
//
//        val token = securityTokenExchangeService.getSystemOidcToken()
//
//        request.headers[HttpHeaders.CONTENT_TYPE] = "application/json"
//        request.headers["Tema"] = "PEN"
//
//        // [Borger, Saksbehandler eller System]
//        request.headers[HttpHeaders.AUTHORIZATION] = "Bearer $userTokan"
//
//        // [System]
//        request.headers["Nav-Consumer-Token"] = "Bearer $token"
//
//

interface PdlToken{
    val systemToken: String
    val userToken: String?
    val isUserToken: Boolean
}

class PdlTokenImp(
    override val systemToken: String,
    override val userToken: String? = null,
    override val isUserToken: Boolean
) : PdlToken