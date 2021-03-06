package no.nav.eessi.pensjon.personoppslag.pdl.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class IdenterResponse(
        val data: IdenterDataResponse? = null,
        val errors: List<ResponseError>? = null
)

internal data class IdenterDataResponse(
        val hentIdenter: HentIdenter
)

internal data class HentIdenter(
        val identer: List<IdentInformasjon>
)

data class IdentInformasjon(
        val ident: String,
        val gruppe: IdentGruppe
)

enum class IdentGruppe {
    AKTORID,
    FOLKEREGISTERIDENT,
    NPID
}

sealed class IdentType(val gruppe: IdentGruppe) {
    override fun toString() = gruppe.name

    object AktoerId : IdentType(IdentGruppe.AKTORID)
    object Npid : IdentType(IdentGruppe.NPID)
    object NorskIdent : IdentType(IdentGruppe.FOLKEREGISTERIDENT)
}

sealed class Ident<T : IdentType> {
    abstract val id: String
}

data class AktoerId(override val id: String) : Ident<IdentType.AktoerId>()
data class NorskIdent(override val id: String) : Ident<IdentType.NorskIdent>()
data class Npid(override val id: String) : Ident<IdentType.Npid>()
