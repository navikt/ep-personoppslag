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
