package no.nav.eessi.pensjon.personoppslag.pdl.model

import com.fasterxml.jackson.annotation.JsonProperty

internal data class GeografiskTilknytningResponse(
        val data: HentGeografiskTilknytning?,
        val errors: List<ResponseError>? = null
)

internal data class HentGeografiskTilknytning(
        @JsonProperty("hentGeografiskTilknytning")
        val geografiskTilknytning: GeografiskTilknytning?
)


data class GeografiskTilknytning(
        val gtType: String,
        val gtKommune: String?,
        val gtBydel: String?,
        val gtLand: String?
)
