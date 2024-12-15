plugins {
    id("java")
}

group = "org.int13h"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.avaje:avaje-inject:11.0")
    annotationProcessor("io.avaje:avaje-inject-generator:11.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}