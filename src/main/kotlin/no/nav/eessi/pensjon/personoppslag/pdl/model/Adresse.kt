package no.nav.eessi.pensjon.personoppslag.pdl.model

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

enum class KontaktadresseType {
        Innland,
        Utland;
}

data class UtenlandskAdresseIFrittFormat(
        val adresselinje1: String? = null,
        val adresselinje2: String? = null,
        val adresselinje3: String? = null,
        val byEllerStedsnavn: String? = null,
        val landkode: String,
        val postkode: String? = null
)

data class PostadresseIFrittFormat(
        val adresselinje1: String? = null,
        val adresselinje2: String? = null,
        val adresselinje3: String? = null,
        val postnummer: String? = null,
)