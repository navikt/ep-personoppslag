package no.nav.eessi.pensjon.personoppslag.pdl.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseGradering.STRENGT_FORTROLIG
import no.nav.eessi.pensjon.personoppslag.pdl.model.AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class AdressebeskyttelseResponseTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun serde() {

        val response = AdressebeskyttelseResponse(
                HentAdressebeskyttelse(
                        listOf(
                                AdressebeskyttelseBolkPerson(AdressebeskyttelsePerson(emptyList())),
                                AdressebeskyttelseBolkPerson(AdressebeskyttelsePerson(listOf(Adressebeskyttelse(STRENGT_FORTROLIG)))),
                                AdressebeskyttelseBolkPerson(AdressebeskyttelsePerson(listOf(Adressebeskyttelse(STRENGT_FORTROLIG_UTLAND))))
                        )
                )
        )

        val json = mapper.writeValueAsString(response)

        val result = mapper.readValue(json, AdressebeskyttelseResponse::class.java)

        assertNotNull(result)
        assertEquals(3, result.data?.hentPersonBolk?.size)

        println(json)
    }

    @Test
    fun deserialize() {
        val json = """
            {
              "data": {
                "hentPersonBolk": [
                  {
                    "person": {
                      "adressebeskyttelse": []
                    }
                  },
                  {
                    "person": null
                  },
                  {
                    "person": {
                      "adressebeskyttelse": [
                        {
                          "gradering": "STRENGT_FORTROLIG_UTLAND"
                        }
                      ]
                    }
                  }
                ]
              },
              "errors": null
            }
        """.trimIndent()

        val result = mapper.readValue(json, AdressebeskyttelseResponse::class.java)

        assertNotNull(result)

        val personer = result.data?.hentPersonBolk!!
        assertEquals(3, personer.size)

        assertTrue(personer[0].person!!.adressebeskyttelse.isEmpty())

        assertNull(personer[1].person)

        assertEquals(1, personer[2].person!!.adressebeskyttelse.size)
        assertEquals(STRENGT_FORTROLIG_UTLAND, personer[2].person!!.adressebeskyttelse.first().gradering)
    }
}
