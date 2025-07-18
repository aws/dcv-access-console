import com.github.spotbugs.snom.SpotBugsTask
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
    `java-library`
    java
    jacoco
    `maven-publish`
    pmd
    id("com.diffplug.spotless") version "6.+"
    id("com.github.spotbugs") version "5.+"
    id("org.springframework.boot") version "3.0.6"
    id("io.freefair.lombok") version "8.0.1"
    id("com.github.jk1.dependency-license-report") version "2.0"
    id("com.github.node-gradle.node") version "7.1.0"
}

licenseReport {
    renderers = arrayOf<ReportRenderer>(InventoryHtmlReportRenderer("report.html", "dcv-access-console-auth-server"),
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

group = "dcv-access-console-auth-server"
version = versionInfo
println("Building version: " + version)

configurations.all {
    // Disable the Tomcat Java server to use Jetty instead
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
}

dependencies {

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web:3.0.6")
    implementation("org.springframework.boot:spring-boot-starter-jetty:3.0.6")
    implementation("org.springframework.boot:spring-boot-starter-security:3.0.6")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server:3.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.1.0")
    testImplementation("org.springframework.security:spring-security-test:6.1.3")

    // Jetty Server
    implementation("org.eclipse.jetty:jetty-server:${project.extra["jettyVersion"]}")

    // Throttling
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")
    implementation("org.springframework.boot:spring-boot-starter-cache:3.0.6")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.5")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        palantirJavaFormat()
    }
}

node {
    version.set("16.14.0")
    download.set(true)
    nodeProjectDir.set(file("${project.projectDir}/src/static-pages"))
}

tasks {
    register<com.github.gradle.node.npm.task.NpmTask>("buildStaticPages") {
        dependsOn("npmInstall")
        args.set(listOf("run", "build"))
    }

    register<Delete>("cleanStaticResources") {
        delete("src/main/resources/static")
    }

    named<ProcessResources>("processResources") {
        dependsOn("copyStaticBuildOutput")
    }

    register<Copy>("copyStaticBuildOutput") {
        dependsOn("cleanStaticResources", "buildStaticPages")
        from("src/static-pages/out")
        into("src/main/resources/static")
    }

    named("build") {
        dependsOn("copyStaticBuildOutput")
    }

    clean {
        doLast {
            mkdir(layout.buildDirectory.dir("reports"))
        }
    }

    test {
        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed")
        }

        finalizedBy(jacocoTestReport)
    }

    /*
    Paths to ignore in code coverage evaluations and reports.
    */
    val excludeFromCoverage = listOf(
        "**/config/**",
        "**/logging/**"
    )

    jacocoTestReport {
        dependsOn(test)
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

    jacocoTestCoverageVerification {
        violationRules {
            // Minimum code coverage required to pass a build, from 0.01 (1%) to 1.00 (100%)
            rule { limit { minimum = BigDecimal.valueOf(0.50) } }
        }
        classDirectories.setFrom(
            sourceSets.main.get().output.asFileTree.matching {
                exclude(excludeFromCoverage)
            }
        )

    }

    withType(SpotBugsTask::class).configureEach {
        reports.register("html") {
            required.set(true)
        }
    }

    check {
        dependsOn(jacocoTestCoverageVerification, spotlessCheck)
    }

    register("fmt") {
        dependsOn("spotlessApply")
    }

    register("release") {
        dependsOn("build", "copyConfig")
    }
}

// Remove from jar
tasks.withType<Jar> {
    exclude("*.properties")
    exclude("logback.xml")
    archiveBaseName.set("dcv-access-console-auth-server")
}

tasks.register<Copy>("copyConfig") {
    dependsOn("processResources")
    from(layout.buildDirectory.file("resources/main/access-console-auth-server.properties"))
    from(layout.buildDirectory.file("resources/main/access-console-auth-server-secrets.properties"))
    from(layout.buildDirectory.file("resources/main/access-console-auth-server-advanced.properties"))
    from(layout.buildDirectory.file("resources/main/dependencies-properties-transform.properties"))
    into(layout.buildDirectory.file("artifacts"))
}

defaultTasks("release")
