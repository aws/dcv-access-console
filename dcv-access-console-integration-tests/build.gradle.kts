plugins {
    `java-library`
    jacoco
    `maven-publish`
    id("io.freefair.lombok") version "8.0.1"
}

group = "dcv-access-console-integration-tests"
version = "0.0"

dependencies {
    implementation("org.testng:testng:7.8.0")

    implementation("org.apache.httpcomponents:httpclient:4.5.14+")
    implementation("org.apache.httpcomponents:httpcore:4.4.16+")
    implementation("org.apache.httpcomponents:httpmime:4.5.14+")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.1")

    implementation("software.amazon.awssdk:ssm:2.21.6")
    implementation("software.amazon.awssdk:ec2:2.21.6")

    implementation("org.projectlombok:lombok:1.18.30+")
    implementation("org.slf4j:slf4j-simple:2.0.11+")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.register<Test>("run-integration") {
    group = "verification"
    useTestNG() {
        suites("src/main/java/testng.xml")
    }
    testClassesDirs = project.sourceSets.main.get().output.classesDirs
    testLogging {
        events("passed", "skipped", "failed")
    }
    outputs.upToDateWhen {false}
}

tasks {
    register("fmt") {
        dependsOn("spotlessApply")
    }

    register("release") {
        dependsOn("build")
    }
}

defaultTasks("release")
