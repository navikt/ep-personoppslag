package no.nav.eessi.pensjon.personoppslag.pdl.model

import com.fasterxml.jackson.annotation.JsonProperty

internal data class PersonResponse(
        val data: HentPerson?,
        val errors: List<ResponseError>? = null
)

internal data class HentPerson(
        @JsonProperty("hentPerson")
        val pdlPerson: PdlPerson?
)

/**
 * TODO: Make internal and create externally used object
 */
data class PdlPerson(
        val adressebeskyttelse: List<Adressebeskyttelse>?,
        val navn: List<Navn>?,
        val statsborgerskap: List<Statsborgerskap>?,
        val foedsel: List<Foedsel>?
)

data class Navn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String
)

data class Statsborgerskap(
        val land: String?,
        val gyldigFraOgMed: String?,
        val gyldigTilOgMed: String?
)

data class Foedsel(
        val foedselsdato: String?
)
