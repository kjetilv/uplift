import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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

//tasks.register("native-image") {
//    project.runCommand(
//        command = Native.image(
//            "uplift-flambda-0.1.1-SNAPSHOT-all.jar",
//            "flambda"
//        )
//    )
//    dependsOn(tasks.named("shadowJar"))
//}
