package no.nav.eessi.pensjon.personoppslag.pdl

import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.GeografiskTilknytningResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.GraphqlRequest
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentPersonResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentPersonUidResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.HentPersonnavnResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdenterResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.SokCriteria
import no.nav.eessi.pensjon.personoppslag.pdl.model.SokPersonGraphqlRequest
import no.nav.eessi.pensjon.personoppslag.pdl.model.SokPersonResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.SokPersonVariables
import no.nav.eessi.pensjon.personoppslag.pdl.model.Variables
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject


@Component
class PersonClient(
    private val pdlRestTemplate: RestTemplate,
    @Value("\${PDL_URL}") private val url: String
) {

    @Retryable(
        exclude = [HttpClientErrorException.NotFound::class],
        backoff = Backoff(delay = 10000L, maxDelay = 100000L, multiplier = 3.0)
    )
    internal fun hentPersonUtenlandsIdent(ident: String): HentPersonUidResponse {
        val query = getGraphqlResource("/graphql/hentPersonUtenlandsIdent.graphql")
        val request = GraphqlRequest(query, Variables(ident))

        return pdlRestTemplate.postForObject(url, HttpEntity(request), HentPersonUidResponse::class)
    }

    /**
     * Oppretter GraphQL Query for uthentig av person
     *
     * @param ident: Personen sin ident (fnr). Legges til som variabel på spørringen.
     *
     * @return GraphQL-objekt [PersonResponse] som inneholder data eller error.
     */
    @Retryable(
        exclude = [HttpClientErrorException.NotFound::class],
        backoff = Backoff(delay = 10000L, maxDelay = 100000L, multiplier = 3.0)
    )
    internal fun hentPerson(ident: String): HentPersonResponse {
        val query = getGraphqlResource("/graphql/hentPerson.graphql")
        val request = GraphqlRequest(query, Variables(ident))
        return pdlRestTemplate.postForObject(url, HttpEntity(request), HentPersonResponse::class)
  }

    @Retryable(
        exclude = [HttpClientErrorException.NotFound::class],
        backoff = Backoff(delay = 10000L, maxDelay = 100000L, multiplier = 3.0)
    )
    internal fun hentPersonnavn(ident: String): HentPersonnavnResponse {
        val query = getGraphqlResource("/graphql/hentPersonnavn.graphql")
        val request = GraphqlRequest(query, Variables(ident))

        return pdlRestTemplate.postForObject(url, HttpEntity(request), HentPersonnavnResponse::class)
    }

    /**
     * Oppretter GraphQL Query for uthentig av adressebeskyttelse
     *
     * @param identer: Liste med person-identer (fnr). Legges til som variabel på spørringen.
     *
     * @return GraphQL-objekt [PersonResponse] som inneholder data eller error.
     */
    @Retryable(
        exclude = [HttpClientErrorException.NotFound::class],
        backoff = Backoff(delay = 10000L, maxDelay = 100000L, multiplier = 3.0)
    )
    internal fun hentAdressebeskyttelse(identer: List<String>): AdressebeskyttelseResponse {
        val query = getGraphqlResource("/graphql/hentAdressebeskyttelse.graphql")
        val request = GraphqlRequest(query, Variables(identer = identer))

        return pdlRestTemplate.postForObject(url, HttpEntity(request), AdressebeskyttelseResponse::class)
    }

    /**
     * Oppretter GraphQL Query for uthentig av en person sin AktørID.
     *
     * @param ident: Personen sin ident (fnr). Legges til som variabel på spørringen.
     *
     * @return GraphQL-objekt [IdenterResponse] som inneholder data eller error.
     */
    @Retryable(
        exclude = [HttpClientErrorException.NotFound::class],
        backoff = Backoff(delay = 10000L, maxDelay = 100000L, multiplier = 3.0)
    )
    internal fun hentAktorId(ident: String): IdenterResponse {
        val query = getGraphqlResource("/graphql/hentAktorId.graphql")
        val request = GraphqlRequest(query, Variables(ident))

        return pdlRestTemplate.postForObject(url, HttpEntity(request), IdenterResponse::class)
    }

    /**
     * Oppretter GraphQL Query for uthentig av en person sine identer.
     * (aktorid, npid, folkeregisterident)
     *
     * @param ident: Personen sin ident (fnr). Legges til som variabel på spørringen.
     *
     * @return GraphQL-objekt [IdenterResponse] som inneholder data eller error.
     */
    @Retryable(
        exclude = [HttpClientErrorException.NotFound::class],
        backoff = Backoff(delay = 10000L, maxDelay = 100000L, multiplier = 3.0)
    )
    internal fun hentIdenter(ident: String): IdenterResponse {
        val query = getGraphqlResource("/graphql/hentIdenter.graphql")
        val request = GraphqlRequest(query, Variables(ident))

        return pdlRestTemplate.postForObject(url, HttpEntity(request), IdenterResponse::class)
    }

    /**
     * Oppretter GraphQL Query for uthentig av en person sin geografiske tilknytning.
     *
     * @param ident: Personen sin ident (fnr). Legges til som variabel på spørringen.
     *
     * @return GraphQL-objekt [GeografiskTilknytningResponse] som inneholder data eller error.
     */

    @Retryable(
        exclude = [HttpClientErrorException.NotFound::class],
        backoff = Backoff(delay = 10000L, maxDelay = 100000L, multiplier = 3.0)
    )
    internal fun hentGeografiskTilknytning(ident: String): GeografiskTilknytningResponse {
        val query = getGraphqlResource("/graphql/hentGeografiskTilknytning.graphql")
        val request = GraphqlRequest(query, Variables(ident))

        return pdlRestTemplate.postForObject(url, HttpEntity(request), GeografiskTilknytningResponse::class)
    }

    @Retryable(
        exclude = [HttpClientErrorException.NotFound::class],
        backoff = Backoff(delay = 10000L, maxDelay = 100000L, multiplier = 3.0)
    )
    internal fun sokPerson(sokCriterias: List<SokCriteria>): SokPersonResponse {
        val query = getGraphqlResource("/graphql/sokPerson.graphql")
        val request = SokPersonGraphqlRequest(query, SokPersonVariables(criteria = sokCriterias))

        return pdlRestTemplate.postForObject(url, HttpEntity(request), SokPersonResponse::class)
    }

    private fun getGraphqlResource(file: String): String =
        javaClass.getResource(file).readText().replace(Regex("[\n\t]"), "")
}
