package no.nav.eessi.pensjon.personoppslag.pdl.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class HentPersonResponse(
    val data: HentPersonResponseData? = null,
    val errors: List<ResponseError>? = null
)

internal data class HentPersonResponseData(
    val hentPerson: HentPerson? = null
)


@JsonIgnoreProperties(ignoreUnknown = true)
internal data class HentPersonUidResponse(
    val data: HentPersonUidResponseData? = null,
    val errors: List<ResponseError>? = null
)

internal data class HentPersonUidResponseData(
    val hentPerson: HentPersonUtenlandskIdent? = null
)
