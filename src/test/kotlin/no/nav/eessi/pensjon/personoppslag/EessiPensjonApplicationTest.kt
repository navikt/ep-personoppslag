package no.nav.eessi.pensjon.personoppslag

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.retry.annotation.EnableRetry
import org.springframework.test.annotation.DirtiesContext

@SpringBootApplication
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc", "no.nav.eessi"])
@Profile("unsecured-webmvctest", "integrationtest")
@DirtiesContext
@EnableRetry
class EessiPensjonApplicationTest
fun main(args: Array<String>) {
	runApplication<EessiPensjonApplicationTest>(*args)
}

