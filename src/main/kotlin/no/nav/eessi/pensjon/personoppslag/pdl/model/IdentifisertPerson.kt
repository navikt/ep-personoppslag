package no.nav.eessi.pensjon.personoppslag.pdl.model

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.eux.model.buc.SakType
import no.nav.eessi.pensjon.shared.person.Fodselsnummer
import java.time.LocalDate
import java.time.Period

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
    @JsonIgnore
    fun isFnrDnrSinFdatoLikSedFdato(): Boolean {
        if (fdato == null) return false
        return fnr?.getBirthDate() == fdato
    }
    @JsonIgnore
    fun alder(): Int? {
        if (fdato == null) return null
        return Period.between(fdato, LocalDate.now()).years
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
