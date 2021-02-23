package no.nav.eessi.pensjon.personoppslag.pdl.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class AdressebeskyttelseResponse(
        val data: HentAdressebeskyttelse? = null,
        val errors: List<ResponseError>? = null
)

internal data class HentAdressebeskyttelse(
        val hentPersonBolk: List<AdressebeskyttelseBolkPerson>? = null
)

data class AdressebeskyttelseBolkPerson(
        val person: AdressebeskyttelsePerson? = null
)

data class AdressebeskyttelsePerson(
        val adressebeskyttelse: List<Adressebeskyttelse>
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