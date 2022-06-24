package no.nav.eessi.pensjon.personoppslag.pdl.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Bostedsadresse(
        val gyldigFraOgMed: LocalDateTime? = null,
        val gyldigTilOgMed: LocalDateTime? = null,
        val vegadresse: Vegadresse? = null,
        val utenlandskAdresse: UtenlandskAdresse? = null,
        val metadata: Metadata
)

typealias Oppholdsadresse = Bostedsadresse

data class Vegadresse(
        val adressenavn: String? = null,
        val husnummer: String? = null,
        val husbokstav: String? = null,
        val postnummer: String? = null,
        val kommunenummer: String? = null,
        val bydelsnummer: String? = null
)

data class UtenlandskAdresse(
        val adressenavnNummer: String? = null,
        val bySted: String? = null,
        val bygningEtasjeLeilighet: String? = null,
        val landkode: String,
        val postboksNummerNavn: String? = null,
        val postkode: String? = null,
        val regionDistriktOmraade: String? = null
)

data class Kontaktadresse(
        val coAdressenavn: String? = null,
        val folkeregistermetadata: Folkeregistermetadata? = null,
        val gyldigFraOgMed: LocalDateTime? = null,
        val gyldigTilOgMed: LocalDateTime? = null,
        val metadata: Metadata,
        val type: KontaktadresseType,
        val utenlandskAdresse: UtenlandskAdresse? = null,
        val utenlandskAdresseIFrittFormat: UtenlandskAdresseIFrittFormat? = null,
        val vegadresse: Vegadresse? = null,
        val postadresseIFrittFormat: PostadresseIFrittFormat? = null
)

/** 
 * Avdøde personer kan ha en kontaktadresse for dødsboet.
 * Kilden kan være Helse (gjerne pårørende) eller tingretten.
 * Det er enten en personSomKontakt eller en advokatSomKontakt
 * eller en organisasjonSomKontakt.
 * Adressen er alltid satt og tilhører kontaktpersonen, advokaten eller
 * organiasjonen.
 */
data class KontaktinformasjonForDoedsbo(
        val personSomKontakt: KontaktinformasjonForDoedsboPersonSomKontakt? = null,
        val advokatSomKontakt: KontaktinformasjonForDoedsboAdvokatSomKontakt? = null,
        val organisasjonSomKontakt: KontaktinformasjonForDoedsboOrganisasjonSomKontakt? = null,
        val adresse: KontaktinformasjonForDoedsboAdresse,
        val attestutstedelsesdato: LocalDate,
        val folkeregistermetadata: Folkeregistermetadata,
        val metadata: Metadata,
        val skifteform: KontaktinformasjonForDoedsboSkifteform
)

/** 
 * Person som er kontakt for dødsbo.
 * Enten er personnavn _eller_ identifikasjonsnummer satt.
 */
data class KontaktinformasjonForDoedsboPersonSomKontakt(
        val personnavn: Personnavn? = null,
        val identifikasjonsnummer: String? = null
)

/** 
 * Advokat som er kontakt for dødsbo.
 * Personnavn finnes alltid, men ikke alltid organisasjonsnavn.
 */
data class KontaktinformasjonForDoedsboAdvokatSomKontakt(
        val personnavn: Personnavn,
        val organisasjonsnavn: String? = null
)

/** 
 * Organisasjon som er kontakt for dødsbo.
 * Organisasjonsnavn finnes alltid, men ikke alltid kontaktnavn.
 */
data class KontaktinformasjonForDoedsboOrganisasjonSomKontakt(
        val kontaktperson: Personnavn? = null,
        val organisasjonsnavn: String
)

data class Personnavn(
        val fornavn: String,
        val mellomnavn: String? = null,
        val etternavn: String
)

enum class KontaktinformasjonForDoedsboSkifteform{
        ANNET,
        OFFENTLIG;
}


data class KontaktinformasjonForDoedsboAdresse(
        val adresselinje1: String,
        val adresselinje2: String? = null,
        val landkode: String? = null,
        val postnummer: String,
        val poststedsnavn: String
)

enum class KontaktadresseType {
        Innland,
        Utland;
}

data class UtenlandskAdresseIFrittFormat(
        val adresselinje1: String? = null,
        val adresselinje2: String? = null,
        val adresselinje3: String? = null,
        val byEllerStedsnavn: String? = null,
        val landkode: String? = null,
        val postkode: String? = null
)

data class PostadresseIFrittFormat(
        val adresselinje1: String? = null,
        val adresselinje2: String? = null,
        val adresselinje3: String? = null,
        val postnummer: String? = null,
)
