package no.nav.eessi.pensjon.pdl

data class GeografiskTilknytning(
        val gtType: String,
        val gtKommune: String?,
        val gtBydel: String?,
        val gtLand: String?
)
