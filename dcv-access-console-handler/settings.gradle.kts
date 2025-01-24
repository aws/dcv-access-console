println("Building with Java version: " + JavaVersion.current())

rootProject.name = "dcv-access-console-handler"

include("dcv-access-console-model")
project(":dcv-access-console-model").projectDir = rootProject.projectDir.parentFile.resolve("dcv-access-console-model")
