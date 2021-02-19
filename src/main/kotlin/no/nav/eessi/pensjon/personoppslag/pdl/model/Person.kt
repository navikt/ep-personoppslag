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
        val familierelasjoner: List<Familierelasjon>,
        val sivilstand: List<Sivilstand>,
        val kontaktadresse: List<Kontaktadresse>
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
        val familierelasjoner: List<Familierelasjon>,
        val sivilstand: List<Sivilstand>,
        val kontaktadresse: Kontaktadresse?
) {
        fun erDoed() = doedsfall?.doedsdato != null
}

data class Navn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
        val forkortetNavn: String?,
        val gyldigFraOgMed: LocalDate?,
        val folkeregistermetadata: Folkeregistermetadata?,
        val metadata: Metadata
        ) {
    val sammensattNavn: String = listOfNotNull(fornavn, mellomnavn, etternavn)
            .joinToString(separator = " ")

    val sammensattEtterNavn: String = listOfNotNull(etternavn, fornavn, mellomnavn)
            .joinToString(separator = " ")
}

data class Statsborgerskap(
        val land: String,
        val gyldigFraOgMed: LocalDate?,
        val gyldigTilOgMed: LocalDate?,
        val metadata: Metadata
)

data class Foedsel(
        val foedselsdato: LocalDate?,
        val foedeland: String?,
        val foedested: String?,
        val foedselsaar: Int?,
        val folkeregistermetadata: Folkeregistermetadata?,
        val metadata: Metadata
)

data class Folkeregistermetadata(
        val gyldighetstidspunkt: LocalDateTime?
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
                        .maxBy { it.registrert }?.registrert!! }
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
        val doedsdato: LocalDate?,
        val folkeregistermetadata: Folkeregistermetadata?,
        val metadata: Metadata
)

enum class Sivilstandstype {
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
        val folkeregistermetadata: Folkeregistermetadata?,
        val metadata: Metadata
)

data class Familierelasjon (
        val relatertPersonsIdent: String,
        val relatertPersonsRolle: Familierelasjonsrolle,
        val minRolleForPerson: Familierelasjonsrolle?,
        val metadata: Metadata
)

data class Sivilstand(
        val type: Sivilstandstype,
        val gyldigFraOgMed: LocalDate?,
        val relatertVedSivilstand: String?,
        val metadata: Metadata
)
