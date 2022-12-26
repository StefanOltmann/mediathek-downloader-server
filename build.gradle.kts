plugins {
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin
    id("io.quarkus") version Versions.quarkus
    id("io.gitlab.arturbosch.detekt") version Versions.detekt
    id("org.sonarqube") version Versions.sonarqube
    id("com.github.ben-manes.versions") version Versions.gradleVersions
    id("de.stefan-oltmann.git-versioning") version Versions.gitVersioning
}

description = "Stefans Mediathek Downloader Server"
group = "de.stefan_oltmann.mediathekdownloader.server"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    kotlinOptions.javaParameters = true
}

detekt {

    config = files("$projectDir/detekt.yml")

    // Don't break the build. Just report.
    ignoreFailures = true
}

sonarqube {
    properties {
        property("sonar.projectKey", "mediathek-downloader-server")
        property("sonar.projectName", "Stefans Mediathek Downloader Server")
        property("sonar.organization", "stefanoltmann")
        property("sonar.host.url", "https://sonarcloud.io")
        // Include Detekt issues
        property("sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.xml")
    }
}

// SonarQube should always contain Detekt issues
tasks.findByPath(":sonarqube")?.dependsOn("detekt")

repositories {
    mavenCentral()
}

dependencies {

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")

    // Quarkus
    implementation(enforcedPlatform("io.quarkus:quarkus-universe-bom:${Versions.quarkus}"))
    implementation("io.quarkus:quarkus-kotlin:${Versions.quarkus}")
    implementation("io.quarkus:quarkus-resteasy-jackson:${Versions.quarkus}")
    implementation("io.quarkus:quarkus-scheduler:${Versions.quarkus}")

    // Logging
    implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
    implementation("ch.qos.logback:logback-classic:${Versions.logback}")

    // XZ compression
    implementation("org.tukaani:xz:${Versions.xz}")

    // Serialisation
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}")

    // Unit tests
    testImplementation(kotlin("test"))

    // Detekt formatting
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detekt}")
}

tasks.test {
    useJUnitPlatform()
}
