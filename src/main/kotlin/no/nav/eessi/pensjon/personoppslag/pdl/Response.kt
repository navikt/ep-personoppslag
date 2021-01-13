package no.nav.eessi.pensjon.personoppslag.pdl

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.eessi.pensjon.personoppslag.pdl.model.PdlPerson

data class PersonResponse(
        val data: HentPerson?,
        val errors: List<ResponseError>? = null
)

data class HentPerson(
        @JsonProperty("hentPerson")
        val pdlPerson: PdlPerson?
)

data class GeografiskTilknytningResponse(
        val data: HentGeografiskTilknytning?,
        val errors: List<ResponseError>? = null
)

data class HentGeografiskTilknytning(
        @JsonProperty("hentGeografiskTilknytning")
        val geografiskTilknytning: GeografiskTilknytning?
)


/**
 * [ResponseError] from GraphQL.
 */
data class ResponseError(
        val message: String?,
        val locations: List<ErrorLocation>? = null,
        val path: List<String>? = null,
        val extensions: ErrorExtension? = null
)

data class ErrorLocation(
        val line: String?,
        val column: String?
)

data class ErrorExtension(
        val code: String?,
        val details: ErrorDetails?,
        val classification: String?
)

data class ErrorDetails(
        val type: String? = null,
        val cause: String? = null,
        val policy: String? = null
)
