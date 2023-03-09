package no.nav.eessi.pensjon.personoppslag.pdl.model

import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.eux.model.buc.SakType
import no.nav.eessi.pensjon.shared.person.Fodselsnummer
import java.time.LocalDate

interface IdentifisertPerson {
    val aktoerId: String                               //fra PDL
    val landkode: String?                              //fra PDL
    val geografiskTilknytning: String?                 //fra PDL
    val personRelasjon: SEDPersonRelasjon?             //fra PDL
    val fnr: Fodselsnummer?
    val personListe: List<IdentifisertPerson>?         //fra PDL){}
    fun flereEnnEnPerson() = personListe != null && personListe!!.size > 1
}

data class SEDPersonRelasjon(
    val fnr: Fodselsnummer?,
    val relasjon: Relasjon,
    val saktype: SakType? = null,
    val sedType: SedType? = null,
    val sokKriterier: SokKriterier? = null,
    val fdato: LocalDate? = null,
    val rinaDocumentId: String
) {
    fun isFnrDnrSinFdatoLikSedFdato(): Boolean {
        if (fdato == null) return true
        return fnr?.getBirthDate() == fdato
    }

}

enum class Relasjon {
    FORSIKRET,
    GJENLEVENDE,
    AVDOD,
    ANNET,
    BARN,
    FORSORGER
}
