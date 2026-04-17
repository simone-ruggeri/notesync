plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.mindrot:jbcrypt:0.4")

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    // ── Dipendenze di test ───────────────────────────────
    // Framework di test Ktor: fornisce testApplication e client HTTP per i test
    //testImplementation("io.ktor:ktor-server-test-host:2.3.12")
    // Client HTTP da usare nei test di integrazione
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    // Framework di asserzioni Kotlin (sostituisce JUnit assertions con sintassi più leggibile)
    testImplementation(kotlin("test"))
    // H2: database in-memoria per i test (evita di dipendere da PostgreSQL nei test)
    testImplementation("com.h2database:h2:2.2.224")
}
