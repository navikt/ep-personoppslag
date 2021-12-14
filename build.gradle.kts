import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    `java-library`
    id("net.researchgate.release") version "2.8.1"
    `maven-publish`
    id("org.sonarqube") version "3.3"
    id("jacoco")
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.10"
    id("com.adarshr.test-logger") version "3.1.0"
    id("com.github.ben-manes.versions") version "0.39.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.18"
    id("org.owasp.dependencycheck") version "6.5.0.1"
    id("com.vanniktech.dependency.graph.generator") version "0.6.0"

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

val springVersion by extra("5.3.13")
val junitVersion by extra("5.8.+")


dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    implementation(kotlin("stdlib", "1.6.10"))
    implementation("org.springframework.boot:spring-boot-starter-web:2.5.3") {
        exclude(module = "tomcat-embed-core")
    }
    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.0.1") {
        exclude("commons-logging", "commons-logging")
    }
    implementation( group = "org.slf4j", name = "jcl-over-slf4j", version = "1.7.32")

    implementation("io.micrometer:micrometer-registry-prometheus:1.4.2")
    // Spring
    implementation("org.springframework:spring-web:$springVersion")
//    implementation("org.springframework.retry:spring-retry:1.3.0")

    implementation("javax.servlet:javax.servlet-api:4.0.1")

    implementation("no.nav.eessi.pensjon:ep-security-sts:0.0.21")
    implementation("no.nav.eessi.pensjon:ep-metrics:0.4.12")
//    implementation(files("../ep-metrics/build/libs/ep-metrics-0.4.12.jar"))

    implementation("no.nav.eessi.pensjon:ep-logging:1.0.15")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.springframework:spring-test:$springVersion")
    testImplementation("io.mockk:mockk:1.12.1")

    //Jackson json
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.1")

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

//https://docs.gradle.org/current/userguide/jacoco_plugin.html
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
