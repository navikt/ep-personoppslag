package no.nav.eessi.pensjon.personoppslag.pdl.model

import java.time.LocalDate
import java.time.LocalDateTime

//@JsonIgnoreProperties(ignoreUnknown = true)
//internal data class PersonResponse<T>(
//        val data: PersonResponseData<T>? = null,
//        val errors: List<ResponseError>? = null
//)
//
//internal data class PersonResponseData<T>(
//        val hentPerson: T? = null
//)

internal data class HentPerson(
        val adressebeskyttelse: List<Adressebeskyttelse>,
        val bostedsadresse: List<Bostedsadresse>,
        val oppholdsadresse: List<Oppholdsadresse>,
        val navn: List<Navn>,
        val statsborgerskap: List<Statsborgerskap>,
        val foedsel: List<Foedsel>,
        val kjoenn: List<Kjoenn>,
        val doedsfall: List<Doedsfall>,
        val forelderBarnRelasjon: List<ForelderBarnRelasjon>,
        val sivilstand: List<Sivilstand>,
        val kontaktadresse: List<Kontaktadresse>?,
        val kontaktinformasjonForDoedsbo: List<KontaktinformasjonForDoedsbo>,
)

internal data class HentPersonUtenlandskIdent(
        val navn: List<Navn>,
        val kjoenn: List<Kjoenn>,
        val utenlandskIdentifikasjonsnummer: List<UtenlandskIdentifikasjonsnummer>
)

data class PersonUtenlandskIdent(
        val identer: List<IdentInformasjon>,
        val navn: Navn? = null,
        val kjoenn: Kjoenn? = null,
        val utenlandskIdentifikasjonsnummer: List<UtenlandskIdentifikasjonsnummer>
)

data class UtenlandskIdentifikasjonsnummer(
        val identifikasjonsnummer: String,
        val utstederland: String,
        val opphoert: Boolean,
        val folkeregistermetadata: Folkeregistermetadata? = null,
        val metadata: Metadata
)

data class Person(
        val identer: List<IdentInformasjon>,
        val navn: Navn? = null,
        val adressebeskyttelse: List<AdressebeskyttelseGradering>,
        val bostedsadresse: Bostedsadresse? = null,
        val oppholdsadresse: Oppholdsadresse? = null,
        val statsborgerskap: List<Statsborgerskap>,
        val foedsel: Foedsel? = null,
        val geografiskTilknytning: GeografiskTilknytning? = null,
        val kjoenn: Kjoenn? = null,
        val doedsfall: Doedsfall? = null,
        val forelderBarnRelasjon: List<ForelderBarnRelasjon>,  //Opplysningen Familierelasjon har byttet navn til ForelderBarnRelasjon
        val sivilstand: List<Sivilstand>,
        val kontaktadresse: Kontaktadresse? = null,
        val kontaktinformasjonForDoedsbo: KontaktinformasjonForDoedsbo? = null
) {
        fun erDoed() = doedsfall?.doedsdato != null
}

data class Navn(
        val fornavn: String,
        val mellomnavn: String? = null,
        val etternavn: String,
        val forkortetNavn: String? = null,
        val gyldigFraOgMed: LocalDate? = null,
        val folkeregistermetadata: Folkeregistermetadata? = null,
        val metadata: Metadata
        ) {
    val sammensattNavn: String = listOfNotNull(fornavn, mellomnavn, etternavn)
            .joinToString(separator = " ")

    val sammensattEtterNavn: String = listOfNotNull(etternavn, fornavn, mellomnavn)
            .joinToString(separator = " ")
}

data class Statsborgerskap(
        val land: String,
        val gyldigFraOgMed: LocalDate? = null,
        val gyldigTilOgMed: LocalDate? = null,
        val metadata: Metadata
)

data class Foedsel(
        val foedselsdato: LocalDate? = null,
        val foedeland: String? = null,
        val foedested: String? = null,
        val foedselsaar: Int? = null,
        val folkeregistermetadata: Folkeregistermetadata? = null,
        val metadata: Metadata
)

data class Folkeregistermetadata(
        val gyldighetstidspunkt: LocalDateTime? = null
)

data class Metadata(
        val endringer: List<Endring>,
        val historisk: Boolean,
        val master: String,
        val opplysningsId: String
) {
        fun sisteRegistrertDato(): LocalDateTime {
                return endringer.maxByOrNull { it.registrert }?.registrert!!
        }
}

data class Endring(
        val kilde: String,
        val registrert: LocalDateTime,
        val registrertAv: String,
        val systemkilde: String,
        val type: Endringstype
)

enum class Endringstype {
        KORRIGER,
        OPPHOER,
        OPPRETT;
}

data class Doedsfall(
        val doedsdato: LocalDate? = null,
        val folkeregistermetadata: Folkeregistermetadata? = null,
        val metadata: Metadata
)

enum class Sivilstandstype {
        UOPPGITT,
        UGIFT,
        GIFT,
        ENKE_ELLER_ENKEMANN,
        SKILT,
        SEPARERT,
        REGISTRERT_PARTNER,
        SEPARERT_PARTNER,
        SKILT_PARTNER,
        GJENLEVENDE_PARTNER;
}

enum class Familierelasjonsrolle {
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
        val kjoenn: KjoennType,
        val folkeregistermetadata: Folkeregistermetadata? = null,
        val metadata: Metadata
)

data class ForelderBarnRelasjon (
        val relatertPersonsIdent: String,
        val relatertPersonsRolle: Familierelasjonsrolle,
        val minRolleForPerson: Familierelasjonsrolle? = null,
        val metadata: Metadata
)

data class Sivilstand(
        val type: Sivilstandstype,
        val gyldigFraOgMed: LocalDate? = null,
        val relatertVedSivilstand: String? = null,
        val metadata: Metadata
)
