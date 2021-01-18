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
        val navn: List<Navn>,
        val statsborgerskap: List<Statsborgerskap>,
        val foedsel: List<Foedsel>,
        val kjoenn: List<Kjoenn>,
        val doedsfall: List<Doedsfall>,
        val familierelasjoner: List<Familierlasjon>,
        val sivilstand: List<Sivilstand>
)

data class Person(
        val identer: List<IdentInformasjon>,
        val navn: Navn?,
        val adressebeskyttelse: List<AdressebeskyttelseGradering>,
        val bostedsadresse: Bostedsadresse?,
        val oppholdsadresse: Oppholdsadresse?,
        val statsborgerskap: List<Statsborgerskap>,
        val foedsel: Foedsel?,
        val geografiskTilknytning: GeografiskTilknytning?,
        val kjoenn: Kjoenn?,
        val doedsfall: Doedsfall?,
        val familierelasjon: List<Familierlasjon>,
        val sivilstand: List<Sivilstand>
) {
        fun erDoed() = doedsfall?.doedsdato != null
}

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

data class Doedsfall(
        val doedsdato: LocalDate?,
        val folkeregistermetadata: Folkeregistermetadata?
)

enum class Sivilstatus {
        UOPPGITT,
        UGIFT,
        GIFT,
        ENKE_ELLER_ENKEMANN,
        SKILT,
        SEPARERT,
        PARTNER,
        SEPARERT_PARTNER,
        SKILT_PARTNER,
        GJENLEVENDE_PARTNER;
}

enum class Relasjon {
        FAR,
        MOR,
        MEDMOR,
        BARN;
}

enum class KjoennType {
        MANN,
        KVINNE,
        UKJENT;
}

data class Kjoenn(
        val kjoenn: KjoennType?,
        val folkeregistermetadata: Folkeregistermetadata?
)

data class Familierlasjon (
        val relatertPersonsIdent: String,
        val relatertPersonsRolle: Relasjon,
        val minRolleForPerson: Relasjon?
)

data class Sivilstand(
        val type: Sivilstatus,
        val gyldigFraOgMed: LocalDate?,
        val relatertVedSivilstand: String?
)
