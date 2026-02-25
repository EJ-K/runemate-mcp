plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.github.javaparser:javaparser-core:3.26.4")
}
