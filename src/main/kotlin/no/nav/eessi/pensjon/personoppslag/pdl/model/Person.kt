package no.nav.eessi.pensjon.personoppslag.pdl.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class PersonResponse(
        val data: PersonResponseData? = null,
        val errors: List<ResponseError>? = null
)

internal data class PersonResponseData(
        val hentPerson: HentPerson? = null
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
        val familierelasjoner: List<Familierelasjon>,
        val sivilstand: List<Sivilstand>,
        val kontaktadresse: List<Kontaktadresse>
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
        val familierelasjoner: List<Familierelasjon>,
        val sivilstand: List<Sivilstand>,
        val kontaktadresse: Kontaktadresse? = null
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
                return endringer.let { endringer
                        .filterNot { it.type == Endringstype.OPPHOER }
                        .maxByOrNull { it.registrert }?.registrert!! }
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

data class Familierelasjon (
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
