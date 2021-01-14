package no.nav.eessi.pensjon.personoppslag.pdl.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class PersonResponse(
        val data: PersonResponseData?,
        val errors: List<ResponseError>? = null
)

internal data class PersonResponseData(
        val hentPerson: HentPerson?
)

internal data class HentPerson(
        val adressebeskyttelse: List<Adressebeskyttelse>,
        val bostedsadresse: List<Bostedsadresse>,
        val oppholdsadresse: List<Oppholdsadresse>,
        val folkeregisteridentifikator: List<Folkeregisteridentifikator>,
        val navn: List<Navn>,
        val statsborgerskap: List<Statsborgerskap>,
        val foedsel: List<Foedsel>
)

data class Person(
        val identer: List<IdentInformasjon>,
        val navn: Navn?,
        val adressebeskyttelse: List<AdressebeskyttelseGradering>?,
        val bostedsadresse: Bostedsadresse?,
        val oppholdsadresse: Oppholdsadresse?,
        val statsborgerskap: List<Statsborgerskap>?,
        val foedsel: Foedsel?
)

data class Folkeregisteridentifikator(
        val identifikasjonsnummer: String,
        val status: String,
        val type: String
)

data class Navn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String
)

data class Statsborgerskap(
        val land: String,
        val gyldigFraOgMed: LocalDate?,
        val gyldigTilOgMed: LocalDate?
)

data class Foedsel(
        val foedselsdato: LocalDate?,
        val folkeregistermetadata: Folkeregistermetadata?
)

data class Folkeregistermetadata(
        val gyldighetstidspunkt: LocalDateTime?
)
