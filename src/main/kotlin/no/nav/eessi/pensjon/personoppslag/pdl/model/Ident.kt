package no.nav.eessi.pensjon.personoppslag.pdl.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.eessi.pensjon.shared.person.Fodselsnummer

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class IdenterResponse(
        val data: IdenterDataResponse? = null,
        val errors: List<ResponseError>? = null
)

internal data class IdenterDataResponse(
        val hentIdenter: HentIdenter? = null
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

sealed class Ident {
    abstract val id: String
    companion object {
        fun bestemIdent(fnrEllerNpid: String): Ident {
            return if (Fodselsnummer.fra(fnrEllerNpid)?.erNpid == true) Npid(fnrEllerNpid)
            else NorskIdent(fnrEllerNpid)
        }
    }
}

data class AktoerId(override val id: String) : Ident()
data class NorskIdent(override val id: String) : Ident()
data class Npid(override val id: String) : Ident()

