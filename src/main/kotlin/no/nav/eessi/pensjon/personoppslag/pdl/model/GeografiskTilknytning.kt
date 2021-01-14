package no.nav.eessi.pensjon.personoppslag.pdl.model

import com.fasterxml.jackson.annotation.JsonProperty

internal data class GeografiskTilknytningResponse(
        val data: GeografiskTilknytningResponseData?,
        val errors: List<ResponseError>? = null
)

internal data class GeografiskTilknytningResponseData(
        @JsonProperty("hentGeografiskTilknytning")
        val geografiskTilknytning: GeografiskTilknytning?
)

data class GeografiskTilknytning(
        val gtType: GtType,
        val gtKommune: String?,
        val gtBydel: String?,
        val gtLand: String?
)

enum class GtType {
        KOMMUNE,
        BYDEL,
        UTLAND,
        UDEFINERT
}
