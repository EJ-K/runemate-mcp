plugins {
    kotlin("jvm") version "2.1.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

apply<com.runemate.mcp.gradle.DocsPlugin>()
apply<com.runemate.mcp.gradle.ApiPlugin>()

group = "com.runemate"
version = "1.1.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://gitlab.com/api/v4/projects/32972353/packages/maven") // runemate-game-api
    maven("https://gitlab.com/api/v4/projects/10471880/packages/maven") // runemate-client
}

dependencies {
    implementation("io.modelcontextprotocol:kotlin-sdk-server:0.8.4")
    implementation("com.runemate:runemate-game-api:1.38.15-SNAPSHOT")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")

    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3") // route SLF4J (from MCP SDK) to Log4j2

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")

    // Runtime dependencies required by runemate-game-api for integration tests
    // (these are normally provided by the RuneMate client at runtime)
    testRuntimeOnly(platform("com.runemate:runemate-client-bom:4.17.7.0"))
    testRuntimeOnly("com.runemate:runemate-client")
    testRuntimeOnly("com.google.guava:guava")
    testRuntimeOnly("org.apache.commons:commons-lang3")
    testRuntimeOnly("org.openjfx:javafx-base:22")
}

application {
    mainClass.set("com.runemate.mcp.MainKt")
}

kotlin {
    jvmToolchain(17)
}
