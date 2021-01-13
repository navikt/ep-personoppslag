package no.nav.eessi.pensjon.personoppslag.pdl.model

import no.nav.eessi.pensjon.personoppslag.pdl.ResponseError

data class IdenterResponse(
        val data: IdenterDataResponse?,
        val errors: List<ResponseError>? = null
)

data class IdenterDataResponse(
        val hentIdenter: HentIdenter
)

data class HentIdenter(
        val identer: List<IdentInformasjon>
)

data class IdentInformasjon(
        val ident: String,
        val gruppe: IdentGruppe,
        val historisk: Boolean
)

enum class IdentGruppe {
    AKTORID,
    FOLKEREGISTERIDENT,
    NPID
}
