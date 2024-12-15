plugins {
    id("java")
}

group = "org.int13h"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation(project(":plink-inject"))
    annotationProcessor(project(":plink-inject"))
    implementation("io.netty:netty-all:4.1.115.Final")
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")


    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-classic:1.5.12")
    implementation("org.apache.httpcomponents.core5:httpcore5:5.3.1")


    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}