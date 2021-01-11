package no.nav.eessi.pensjon.pdl

import no.nav.eessi.pensjon.security.sts.STSService
import org.springframework.stereotype.Service

@Service
class PersonService(
        private val client: PersonClient,
        private val stsService: STSService
) {

    /**
     * @param fnr: Fødselsnummeret til personen man vil hente ut [GeografiskTilknytning] for.
     *
     * @return [GeografiskTilknytning]
     */
    fun hentPerson(fnr: String): PdlPerson? {
        val token = stsService.getSystemOidcToken()

        val response = client.hentPerson(fnr, token)

        if (!response.errors.isNullOrEmpty()) {
            val errorMsg = response.errors.joinToString { it.message ?: "" }
            throw Exception(errorMsg)
        }

        return response.data?.pdlPerson
    }

    /**
     * @param fnr: Fødselsnummeret til personen man vil hente ut Aktør IDen til.
     *
     * @return AktørID som [String]
     */
    fun hentAktorId(fnr: String): String? {
        return hentIdenter(fnr).firstOrNull { it.gruppe == IdentGruppe.AKTORID }?.ident
    }

    /**
     * @param fnr: Fødselsnummeret til personen man vil hente ut [GeografiskTilknytning] for.
     *
     * @return Liste med [IdentInformasjon]
     */
    fun hentIdenter(fnr: String): List<IdentInformasjon> {
        val token = stsService.getSystemOidcToken()

        val response = client.hentIdenter(fnr, token)

        if (!response.errors.isNullOrEmpty()) {
            val errorMsg = response.errors.joinToString { it.message ?: "" }
            throw Exception(errorMsg)
        }

        return response.data?.identer ?: emptyList()
    }

    /**
     * @param fnr: Fødselsnummeret til personen man vil hente ut [GeografiskTilknytning] for.
     *
     * @return [GeografiskTilknytning]
     */
    fun hentGeografiskTilknytning(fnr: String): GeografiskTilknytning? {
        val token = stsService.getSystemOidcToken()

        val response = client.hentGeografiskTilknytning(fnr, token)

        if (!response.errors.isNullOrEmpty()) {
            val errorMsg = response.errors.joinToString { it.message ?: "" }
            throw Exception(errorMsg)
        }

        return response.data?.geografiskTilknytning
    }
}
