package no.nav.eessi.pensjon.personoppslag.aktoerregister

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class EmptyRestTemplateConfig {


    @ConditionalOnMissingBean(name=["aktoerregisterRestTemplate"])
    @Bean
    fun aktoerregisterRestTemplate() = RestTemplate()
}
