# ep-personoppslag

## Releasing

This library is released using the `net.researchgate/gradle-release`-plugin.

## Oppdatere avhengigheter

Det er viktig at man holder avhengigheter oppdatert for å unngå sikkerhetshull.

Se mer dokumentasjon rundt dette her: [Oppgradere avhengigheter](https://github.com/navikt/eessi-pensjon/blob/master/docs/dev/oppgradere_avhengigheter.md).

## SonarQube m/JaCoCo

Prosjektet er satt opp med støtte for å kunne kjøre SonarQube, med JaCoCo for å fange test coverage, men du trenger å ha en SonarQube-instans (lokal?) å kjøre dataene inn i - [les mer her](https://github.com/navikt/eessi-pensjon/blob/master/docs/dev/sonarqube.md).

## Snyk CLI

Siden Snyk ikke støtter Gradle sin Kotlin DSL må sjekker kjøres fra kommandolinjen.
Se: https://support.snyk.io/hc/en-us/articles/360003812458-Getting-started-with-the-CLI
