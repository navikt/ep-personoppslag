import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    `java-library`
    id("net.researchgate.release") version "2.8.1"
    `maven-publish`
    id("org.sonarqube") version "2.8"
    id("jacoco")
    id("com.adarshr.test-logger") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.72"
    id("com.github.ben-manes.versions") version "0.28.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.13"
    id("org.owasp.dependencycheck") version "5.3.2.1"
}

group = "no.nav.eessi.pensjon"

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val springVersion by extra("5.2.5.RELEASE")
val junitVersion by extra("5.6.2")
val cxfVersion by extra("3.3.6")


dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib", "1.3.72"))

    implementation("io.micrometer:micrometer-registry-prometheus:1.4.2")
    implementation("no.nav.eessi.pensjon:ep-security-sts:0.0.8")
    implementation("no.nav.eessi.pensjon:ep-metrics:0.4.1")

    // Spring
    implementation("org.springframework:spring-web:$springVersion")
    implementation("org.springframework.retry:spring-retry:1.3.0")

    implementation("javax.servlet:javax.servlet-api:4.0.1")
    implementation("no.nav.eessi.pensjon:ep-logging:0.0.16")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.springframework:spring-test:$springVersion")
    testImplementation("io.mockk:mockk:1.10.0")

    //Jackson json
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.1")

    // Tjenestespesifikasjoner
    implementation("no.nav.tjenestespesifikasjoner:person-v3-tjenestespesifikasjon:1.2020.01.30-14.36-cdf257baea96")
    implementation("com.sun.xml.ws:jaxws-ri:2.3.2")

    // Apache CXF
    implementation("org.apache.cxf:cxf-spring-boot-starter-jaxws:${cxfVersion}")
    implementation("org.apache.cxf:cxf-rt-ws-security:${cxfVersion}")

    //Mock
    testImplementation("org.mockito:mockito-junit-jupiter:3.3.3")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}

// https://github.com/researchgate/gradle-release
release {
    newVersionCommitMessage = "[Release Plugin] - next version commit: "
    tagTemplate = "release-\${version}"
}

// https://help.github.com/en/actions/language-and-framework-guides/publishing-java-packages-with-gradle#publishing-packages-to-github-packages
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/navikt/${rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

repositories {
    mavenCentral()

    listOf("ep-metrics", "ep-logging", "ep-security-sts").forEach { repo ->
        val token = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key")
        ?: throw NullPointerException("Missing token, you have to set GITHUB_TOKEN or gpr.key, see README")
        maven {
            url = uri("https://maven.pkg.github.com/navikt/$repo")
            credentials {
                username = "token"
                password = token as String?
            }
        }
    }
}

// https://docs.gradle.org/current/userguide/jacoco_plugin.html
jacoco {
    toolVersion = "0.8.5"
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
    }
}

tasks.named("sonarqube") {
    dependsOn("jacocoTestReport")
}

/* https://github.com/ben-manes/gradle-versions-plugin */
tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        listOf("alpha", "beta", "rc", "cr", "m", "preview", "pr2")
                .any { qualifier -> """(?i).*[.-]${qualifier}[.\d-]*""".toRegex().matches(candidate.version) }
    }
    revision = "release"
}
