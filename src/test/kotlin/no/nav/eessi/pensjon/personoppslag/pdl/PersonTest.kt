package no.nav.eessi.pensjon.personoppslag.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import no.nav.eessi.pensjon.personoppslag.pdl.model.KjoennType
import no.nav.eessi.pensjon.personoppslag.pdl.model.PersonResponse
import no.nav.eessi.pensjon.personoppslag.pdl.model.Sivilstatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate


@ExtendWith(MockitoExtension::class)
class PersonTest {

    @Test
    fun hentPersonFraGraphql() {

        val mapper = ObjectMapper()
            .registerModule(KotlinModule())
            .registerModule(ParameterNamesModule())
//            .registerModule(Jdk8Module())
            .registerModule(JavaTimeModule())

        val response = mapper.readValue(mockGraphQlresponse(), PersonResponse::class.java)

        val hentPerson = response.data?.hentPerson!!

        assertEquals("BLÅ", hentPerson.navn.firstOrNull()?.fornavn)
        assertEquals("STAUDE", hentPerson.navn.firstOrNull()?.etternavn)

        assertEquals(LocalDate.of(1985,6, 11), hentPerson.foedsel.firstOrNull()?.foedselsdato)
        assertEquals(Sivilstatus.GIFT, hentPerson.sivilstand.firstOrNull()?.type)
        assertEquals(KjoennType.KVINNE, hentPerson.kjoenn.firstOrNull()?.kjoenn)

    }

    fun mockGraphQlresponse() : String {
        return """
{
    "data": {
        "hentPerson": {
            "adressebeskyttelse": [],
            "bostedsadresse": [
                {
                    "vegadresse": {
                        "adressenavn": "SANNERGATA",
                        "husnummer": "6",
                        "husbokstav": null,
                        "postnummer": "0557"
                    },
                    "utenlandskAdresse": null
                }
            ],
            "oppholdsadresse": [
                {
                    "vegadresse": {
                        "adressenavn": "SANNERGATA",
                        "husnummer": "6",
                        "husbokstav": null,
                        "postnummer": "0557"
                    },
                    "utenlandskAdresse": null
                }
            ],
            "navn": [
                {
                    "fornavn": "BLÅ",
                    "mellomnavn": null,
                    "etternavn": "STAUDE"
                }
            ],
            "statsborgerskap": [
                {
                    "land": "NOR",
                    "gyldigFraOgMed": "1985-06-11",
                    "gyldigTilOgMed": null
                }
            ],
            "foedsel": [
                {
                    "foedselsdato": "1985-06-11",
                    "folkeregistermetadata": {
                        "gyldighetstidspunkt": "2021-01-14T15:42:45"
                    }
                }
            ],
            "doedsfall": [],
            "kjoenn": [
                {
                    "kjoenn": "KVINNE",
                    "folkeregistermetadata": {
                        "gyldighetstidspunkt": "2021-01-14T15:42:45"
                    }
                }
            ],
            "familierelasjoner": [
                {
                    "relatertPersonsIdent": "20081779660",
                    "relatertPersonsRolle": "BARN",
                    "minRolleForPerson": "MOR"
                }
            ],
            "sivilstand": [
                {
                    "type": "GIFT",
                    "gyldigFraOgMed": "2010-01-25",
                    "relatertVedSivilstand": "03128222382"
                }
            ]
        }
    }
}
        """.trimIndent()
    }

}