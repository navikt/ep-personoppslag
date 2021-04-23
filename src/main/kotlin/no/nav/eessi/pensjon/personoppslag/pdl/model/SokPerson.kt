package no.nav.eessi.pensjon.personoppslag.pdl.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

data class SokKriterier(
    val fornavn: String,
    val etternavn: String,
    val foedselsdato: LocalDate
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class SokPersonResponse(
    val data: SokPersonData? = null,
    val errors: List<ResponseError>? = null
)

internal data class SokPersonGraphqlRequest(
    val query: String,
    val variables: SokPersonVariables
)

internal data class SokPersonData(
    val sokPerson: SokPerson? = null,
)

internal data class SokPerson(
    val pageNumber: Int,
    val totalHits: Int,
    val totalPages: Int,
    val hits: List<PersonSearchHit>
)

internal data class PersonSearchHit(
    val identer: List<IdentInformasjon>
)

internal data class SokPersonVariables(
    val paging: SokPaging = SokPaging(),
    val criteria: List<SokCriteria>
)

internal data class SokPaging(
    val pageNumber: Int = 1,
    val resultsPerPage: Int = 10
)

internal data class SokCriteria(
    val fieldName: String,
    val searchRule: Map<String, String>
)
