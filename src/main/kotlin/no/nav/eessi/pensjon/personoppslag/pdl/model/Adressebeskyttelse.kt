package no.nav.eessi.pensjon.personoppslag.pdl.model

import no.nav.eessi.pensjon.personoppslag.pdl.ResponseError

data class AdressebeskyttelseResponse(
        val data: HentAdressebeskyttelse?,
        val errors: List<ResponseError>? = null
)

data class HentAdressebeskyttelse(
        val hentPersonBolk: List<AdressebeskyttelseBolkPerson>?
)

data class AdressebeskyttelseBolkPerson(
        val person: AdressebeskyttelsePerson
)

data class AdressebeskyttelsePerson(
        val adressebeskyttelse: List<Adressebeskyttelse>?
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