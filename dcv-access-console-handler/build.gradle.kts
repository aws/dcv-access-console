import org.apache.tools.ant.taskdefs.condition.Os
import java.io.ByteArrayOutputStream
import com.github.jk1.license.render.ReportRenderer
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.CsvReportRenderer
import com.github.jk1.license.render.TextReportRenderer
import java.io.File
import com.google.gson.Gson
import com.google.gson.JsonObject

apply(from = "config.gradle.kts")
repositories {
    mavenCentral()
}

buildscript {
    dependencies {
        classpath("com.palantir.javaformat:gradle-palantir-java-format:2.+")
        classpath("com.google.code.gson:gson:2.8.9")
    }
}

plugins {
    java
    jacoco
    `java-library`
    `maven-publish`
    id("org.springframework.boot") version "3.1.0"
    id("io.freefair.lombok") version "8.0.1"
    id("org.javacc.javacc") version "3.0.0"
    id("com.github.jk1.dependency-license-report") version "2.0"
}

licenseReport {
    renderers = arrayOf<ReportRenderer>(InventoryHtmlReportRenderer("report.html", "dcv-access-console-handler"),
        CsvReportRenderer("third-party-libs.csv"), TextReportRenderer("third-party-licenses.txt"))
}

val dcvtoolPackage = projectDir.parentFile.resolve("dcvtool.package.json")
val versionInfo = if (dcvtoolPackage.exists()) {
    val jsonString = dcvtoolPackage.readText()
    val jsonObject = Gson().fromJson(jsonString, JsonObject::class.java)
    val versionObject = jsonObject.getAsJsonObject("version")
    "${versionObject.get("major").asInt}.${versionObject.get("minor").asInt}"
} else {
    "0.0" // Default version if file doesn't exist
}

group = "dcv-access-console-handler"
version = versionInfo
println("Building version: " + version)

configurations.all {
    // Disable the Tomcat Java server to use Jetty instead
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
}

dependencies {

    // DCV Access Console Model
    implementation(project(":dcv-access-console-model"))

    // JUnit
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.1.0")
    testImplementation("org.springframework.security:spring-security-test:6.1.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.assertj:assertj-core:3.25.3")

    // Lombok
    implementation("org.projectlombok:lombok:1.18.26")

    // Swagger
    implementation("io.swagger.core.v3:swagger-core:2.2.4")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.+")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web:3.0.6")
    implementation("org.springframework.boot:spring-boot-starter-jetty:3.0.6")
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.0.6")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.1.2")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.0")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:3.1.3")

    // Jetty Server
    implementation("org.eclipse.jetty:jetty-server:${project.extra["jettyVersion"]}")

    // Jackson XML
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.1")

    // OK HTTP
    implementation("com.squareup.okhttp:okhttp:2.7.5")
    implementation("com.squareup.okhttp:logging-interceptor:2.7.5")

    // GSON
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("io.gsonfire:gson-fire:1.8.+")

    // HTTP Client
    implementation("org.apache.httpcomponents:httpclient:4.5.+")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    // DynamoDb
    implementation(platform("software.amazon.awssdk:bom:2.20.114"))
    implementation("software.amazon.awssdk:dynamodb-enhanced")
    implementation("software.amazon.awssdk:dynamodb")

    //Guava
    implementation("com.google.guava:guava:r05")

    // Broker Client
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // Throttling
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")
    implementation("org.springframework.boot:spring-boot-starter-cache:3.0.6")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.5")

    //JavaTuples
    implementation("org.javatuples:javatuples:1.2")

    //Cedar
    implementation("com.google.code.findbugs:jFormatString:3.0.0")
    implementation("com.cedarpolicy:cedar-java:4.2.2:uber") {
        exclude(group = "com.apple", module = "AppleJavaExtensions")
    }

    //OpenCSV
    implementation("com.opencsv:opencsv:5.9")
}

sourceSets.main {
    java.srcDirs("src/main/java", "${layout.buildDirectory.get()}/generated/javacc")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
/*
Paths to ignore in code coverage evaluations and reports.
*/
val excludeFromCoverage = listOf(
    "**/config/**",
    "**/repositories/**",
    "**/persistence/**",
    "**/requirements/**",
    "**/authorization/**"
)

tasks {
    compileJavacc {
        arguments = mutableMapOf(
            "grammar_encoding" to "UTF-8",
            "static" to "false"
        )
    }

    jacocoTestCoverageVerification {
        violationRules {
            // Set minimum code coverage to fail build, where 0.01 = 1%.
            rule { limit { minimum = BigDecimal.valueOf(0.80) } } // TODO: Set back to 0.90
        }
        classDirectories.setFrom(
            sourceSets.main.get().output.asFileTree.matching {
                exclude(excludeFromCoverage)
            }
        )
    }
    check {
        dependsOn(jacocoTestCoverageVerification)
    }
    test {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }
    jacocoTestReport {
        reports {
            csv.required.set(true)
            xml.required.set(true)
            xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/test/coverage.xml"))
            classDirectories.setFrom(
                sourceSets.main.get().output.asFileTree.matching {
                    exclude(excludeFromCoverage)
                })
        }
    }

    register<Jar>("sourceJar") {
        from(java.sourceSets["main"].java.srcDirs)
    }

    register("release") {
        dependsOn("build", "copyConfig")
    }
}

// Remove from jar
tasks.withType<Jar> {
    exclude("*.properties")
    exclude("logback-spring.xml")
    archiveBaseName.set("dcv-access-console-handler")
}

tasks.register<Copy>("copyConfig") {
    dependsOn("processResources")
    from(layout.buildDirectory.file("resources/main/access-console-handler.properties"))
    from(layout.buildDirectory.file("resources/main/access-console-handler-secrets.properties"))
    from(layout.buildDirectory.file("resources/main/access-console-handler-advanced.properties"))
    into(layout.buildDirectory.file("artifacts"))
}

defaultTasks("release")
