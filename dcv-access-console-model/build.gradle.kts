import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.io.ByteArrayOutputStream
import com.github.jk1.license.render.ReportRenderer
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.CsvReportRenderer
import com.github.jk1.license.render.TextReportRenderer

group = "dcv-access-console-model"
version = "0.0"

repositories {
    mavenCentral()
}

buildscript {
    dependencies {
        classpath("com.palantir.javaformat:gradle-palantir-java-format:2.+")
    }
}

plugins {
    java
    `java-library`
    `maven-publish`
    id("com.github.jk1.dependency-license-report") version "2.0"
    id("org.openapi.generator") version("7.0.0")
}
licenseReport {
    renderers = arrayOf<ReportRenderer>(InventoryHtmlReportRenderer("report.html", "dcv-access-console-model"),
        CsvReportRenderer("third-party-libs.csv"), TextReportRenderer("third-party-licenses.txt"))
}

dependencies {

    // JUnit
    implementation("org.junit.jupiter:junit-jupiter-api:5.10.0")

    //OK HTTP
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // Swagger
    compileOnly("io.swagger.core.v3:swagger-annotations:2.2.20")

    //Spring
    compileOnly("org.springframework.boot:spring-boot-starter-web:3.0.6")

    // GSON
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("io.gsonfire:gson-fire:1.8.5")

    // Jakarta
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    //DynamoDB
    implementation(platform("software.amazon.awssdk:bom:2.20.114"))
    implementation("software.amazon.awssdk:dynamodb-enhanced")

    //OpenAPI
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
}

val generatedSrcPath = "${layout.buildDirectory.get()}/generated-src"

tasks {
    compileJava {
        dependsOn("smBrokerModel")
        dependsOn("smModel")
        dependsOn("smModelHtml")
        sourceSets.main {
            java {
                srcDir(generatedSrcPath)
            }
            resources {
                srcDir("$projectDir/model")
            }
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    register("moveJar") {
        dependsOn("jar")
        doLast {
            val jarFile = File("${layout.buildDirectory.get()}/libs/${project.name}-${project.version}.jar")
            val targetDir = File("${layout.buildDirectory.get()}/artifacts")
            targetDir.mkdirs()
            jarFile.copyTo(File(targetDir, jarFile.name), overwrite = true)
        }
    }
}

tasks.register<GenerateTask>("smBrokerModel") {
    generatorName.set("java")
    inputSpec.set("$projectDir/model/dcv-session-manager-api.yaml")
    outputDir.set("$generatedSrcPath/broker")
    modelPackage.set("broker.model")
    apiPackage.set("broker.api")
    cleanupOutput.set(true)
    skipOperationExample.set(true)
    configOptions.set(mapOf(
        "sourceFolder" to "", // This ensures a src/main/java isn't prepended to the source in the generated-src folder
        "useJakartaEe" to "true"
    ))
}

tasks.register<GenerateTask>("smModel") {
    generatorName.set("spring")
    inputSpecRootDirectory.set("$projectDir/model/dcv-session-manager-api.yaml")
    inputSpec.set("$projectDir/model/dcv-access-console-handler-api.yaml")
    outputDir.set("$generatedSrcPath/handler")
    modelPackage.set("handler.model")
    apiPackage.set("handler.api")
    cleanupOutput.set(true)
    skipOperationExample.set(true)
    configOptions.set(mapOf(
        "sourceFolder" to "", // This ensures a src/main/java isn't prepended to the source in the generated-src folder
        "interfaceOnly" to "true",
        "openApiNullable" to "false",
        "useBeanValidation" to "false",
        "useSpringBoot3" to "true"
    ))
}

tasks.register<GenerateTask>("smModelHtml") {
    generatorName.set("html2")
    inputSpec.set("$projectDir/model/dcv-access-console-handler-api.yaml")
    outputDir.set("${layout.buildDirectory.get()}/html2/")
}

tasks.register<Copy>("copyModel") {
    dependsOn("processResources")
    from("$projectDir/model/dcv-session-manager-api.yaml")
    from("$projectDir/model/dcv-access-console-handler-api.yaml")
    into(layout.buildDirectory.file("artifacts"))
}

tasks.register("release") {
    dependsOn("compileJava", "build", "copyModel", "moveJar")
}

defaultTasks("release")
