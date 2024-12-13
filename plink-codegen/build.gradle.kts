plugins {
    id("java")
}

group = "org.int13h"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.avaje:avaje-prisms:1.36")
    annotationProcessor("io.avaje:avaje-prisms:1.36")
    compileOnly("jakarta.inject:jakarta.inject-api:2.0.1")

    implementation("com.palantir.javapoet:javapoet:0.6.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}