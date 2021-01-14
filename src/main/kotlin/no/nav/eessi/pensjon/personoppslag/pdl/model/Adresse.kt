package no.nav.eessi.pensjon.personoppslag.pdl.model

import java.time.LocalDateTime

data class Bostedsadresse(
        val gyldigFraOgMed: LocalDateTime,
        val gyldigTilOgMed: LocalDateTime,
        val vegadresse: Vegadresse?,
        val utenlandskAdresse: UtenlandskAdresse?
)

typealias Oppholdsadresse = Bostedsadresse

data class Vegadresse(
        val adressenavn: String?,
        val husnummer: String?,
        val husbokstav: String?,
        val postnummer: String?
)

data class UtenlandskAdresse(
        val landkode: String
)
