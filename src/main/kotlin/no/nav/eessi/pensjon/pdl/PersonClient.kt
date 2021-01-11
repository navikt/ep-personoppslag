package no.nav.eessi.pensjon.pdl

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

@Component
class PersonClient(
        private val restTemplate: RestTemplate,
        @Value("\${PDL_URL}") private val pdlUrl: String
) {

    /**
     * Oppretter GraphQL Query for uthentig av person
     *
     * @param ident: Personen sin ident (fnr). Legges til som variabel på spørringen.
     *
     * @return GraphQL-objekt [PersonResponse] som inneholder data eller error.
     */
    fun hentPerson(ident: String, token: String): PersonResponse {
        val query = getGraphqlResource("/graphql/hentPerson.graphql")
        val request = GraphqlRequest(query, Variables(ident))
        val entity = createRequestEntity(request, token)

        return restTemplate.postForObject(pdlUrl, entity, PersonResponse::class)
    }

    /**
     * Oppretter GraphQL Query for uthentig av en person sine identer.
     * (aktorid, npid, folkeregisterident)
     *
     * @param ident: Personen sin ident (fnr). Legges til som variabel på spørringen.
     *
     * @return GraphQL-objekt [IdenterResponse] som inneholder data eller error.
     */
    fun hentIdenter(ident: String, token: String): IdenterResponse {
        val query = getGraphqlResource("/graphql/hentIdenter.graphql")
        val request = GraphqlRequest(query, Variables(ident))
        val entity = createRequestEntity(request, token)

        return restTemplate.postForObject(pdlUrl, entity, IdenterResponse::class)
    }

    /**
     * Oppretter GraphQL Query for uthentig av en person sin geografiske tilknytning.
     *
     * @param ident: Personen sin ident (fnr). Legges til som variabel på spørringen.
     *
     * @return GraphQL-objekt [GeografiskTilknytningResponse] som inneholder data eller error.
     */
    fun hentGeografiskTilknytning(ident: String, token: String): GeografiskTilknytningResponse {
        val query = getGraphqlResource("/graphql/hentGeografiskTilknytning.graphql")
        val request = GraphqlRequest(query, Variables(ident))
        val entity = createRequestEntity(request, token)

        return restTemplate.postForObject(pdlUrl, entity, GeografiskTilknytningResponse::class)
    }

    private fun createRequestEntity(request: GraphqlRequest, token: String): HttpEntity<Any> {
        val headers = HttpHeaders()
        headers[HttpHeaders.CONTENT_TYPE] = "application/json"
        headers[HttpHeaders.AUTHORIZATION] = "Bearer $token"
        headers["Nav-Consumer-Token"] = "Bearer $token"
        headers["Tema"] = "PEN"

        return HttpEntity(request, headers)
    }

    private fun getGraphqlResource(file: String): String =
            javaClass.getResource(file).readText().replace(Regex("[\n\t]"), "")
}
