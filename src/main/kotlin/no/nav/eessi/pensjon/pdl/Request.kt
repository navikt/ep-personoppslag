package no.nav.eessi.pensjon.pdl

data class GraphqlRequest(
        val query: String,
        val variables: Variables
)

data class Variables(
        val ident: String
)
