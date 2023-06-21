import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.kjetilv.uplift.plugins.NativeLamdbdaTask

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.kjetilv.uplift.plugins.native") version "0.1.1-SNAPSHOT"
}

dependencies {
    implementation("com.github.kjetilv.uplift:uplift-kernel:0.1.1-SNAPSHOT")
    implementation("com.github.kjetilv.uplift:uplift-lambda:0.1.1-SNAPSHOT")

    testImplementation("com.github.kjetilv.uplift:uplift-flambda:0.1.1-SNAPSHOT")
    testImplementation("com.github.kjetilv.uplift:uplift-asynchttp:0.1.1-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<ShadowJar> {
    manifest {
        attributes(mapOf(Pair("Main-Class", "uplift.examples.helloweb.HelloWeb")))
    }
    mergeServiceFiles()
    minimize()
    dependsOn("build")
}

tasks.withType<NativeLamdbdaTask> {
    dependsOn("shadowJar")
}
