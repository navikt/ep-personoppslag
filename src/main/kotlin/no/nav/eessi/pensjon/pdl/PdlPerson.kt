package no.nav.eessi.pensjon.pdl

import com.fasterxml.jackson.annotation.JsonProperty

data class PdlPerson(
        val adressebeskyttelse: List<Adressebeskyttelse>?,
        val navn: List<Navn>?,
        val statsborgerskap: List<Statsborgerskap>?,
        val foedsel: List<Foedsel>?
)

data class Adressebeskyttelse(
        val gradering: AdressebeskyttelseGradering
)

enum class AdressebeskyttelseGradering {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT
}

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
