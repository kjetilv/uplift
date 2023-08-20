import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(project(":uplift-flogs"))

    implementation(project(":uplift-kernel"))
    implementation(project(":uplift-lambda"))
    implementation(project(":uplift-json"))
    implementation(project(":uplift-asynchttp"))

    testImplementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")

    implementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes(mapOf(Pair("Main-Class", "com.github.kjetilv.uplift.flambda.Main")))
    }
    mergeServiceFiles()
    minimize()
}

tasks.register("native-image") {
    val jarFile = "uplift-flambda-0.1.1-SNAPSHOT-all.jar"
    val binary = "flambda"
    val cmd = """
            ${javaBin("native-image")} 
             --verbose 
             --no-fallback
             -H:+ReportExceptionStackTraces
             -H:+BuildReport
             --enable-url-protocols=https 
             -jar $jarFile
             -o $binary
             -march=native
            """
    val libsDir = project.layout.buildDirectory.dir("libs").get().asFile.also {
        Files.createDirectories(it.toPath())
    }
    project.exec {
        workingDir = libsDir
        commandLine = cmd.trimIndent().split("\\s+".toRegex())
    }
    dependsOn(tasks.named("shadowJar"))
}

fun javaBin(name: String) =
    System.getProperty("java.home").asPath().resolve("bin").resolve(name)

fun String.asPath() = Paths.get(this)

fun Path.isExecutable() = Files.isExecutable(this)
