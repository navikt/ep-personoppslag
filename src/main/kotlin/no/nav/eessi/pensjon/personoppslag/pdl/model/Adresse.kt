package no.nav.eessi.pensjon.personoppslag.pdl.model

import java.time.LocalDateTime

data class Bostedsadresse(
        val gyldigFraOgMed: LocalDateTime?,
        val gyldigTilOgMed: LocalDateTime?,
        val vegadresse: Vegadresse?,
        val utenlandskAdresse: UtenlandskAdresse?,
        val metadata: Metadata
)

typealias Oppholdsadresse = Bostedsadresse

data class Vegadresse(
        val adressenavn: String?,
        val husnummer: String?,
        val husbokstav: String?,
        val postnummer: String?,
        val kommunenummer: String?,
        val bydelsnummer: String?
)

data class UtenlandskAdresse(
        val adressenavnNummer: String?,
        val bySted: String?,
        val bygningEtasjeLeilighet: String?,
        val landkode: String,
        val postboksNummerNavn: String?,
        val postkode: String?,
        val regionDistriktOmraade: String?
)

data class Kontaktadresse(
        val coAdressenavn: String?,
        val folkeregistermetadata: Folkeregistermetadata?,
        val gyldigFraOgMed: LocalDateTime?,
        val gyldigTilOgMed: LocalDateTime?,
        val metadata: Metadata,
        val type: KontaktadresseType,
        val utenlandskAdresse: UtenlandskAdresse?,
        val utenlandskAdresseIFrittFormat: UtenlandskAdresseIFrittFormat?,
        val vegadresse: Vegadresse?,
        val postadresseIFrittFormat: PostadresseIFrittFormat?
)

enum class KontaktadresseType {
        Innland,
        Utland;
}

data class UtenlandskAdresseIFrittFormat(
        val adresselinje1: String?,
        val adresselinje2: String?,
        val adresselinje3: String?,
        val byEllerStedsnavn: String?,
        val landkode: String,
        val postkode: String?
)

data class PostadresseIFrittFormat(
        val adresselinje1: String?,
        val adresselinje2: String?,
        val adresselinje3: String?,
        val postnummer: String?,
)