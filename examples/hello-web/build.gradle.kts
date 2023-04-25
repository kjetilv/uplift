import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.kjetilv.uplift.plugins.NativeLambdaPlugin
import com.github.kjetilv.uplift.plugins.UpliftPlugin
import com.github.kjetilv.uplift.plugins.UpliftTask

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.0"
    id("com.github.kjetilv.uplift.plugins.uplift") version "0.1.0-SNAPSHOT"
    id("com.github.kjetilv.uplift.plugins.native") version "0.1.0-SNAPSHOT"
}

repositories{
    mavenLocal()
    mavenCentral()
}

group = "com.github.kjetilv.uplift.examples"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation("com.github.kjetilv.uplift:uplift-kernel:0.1.0-SNAPSHOT")
    implementation("com.github.kjetilv.uplift:uplift-lambda:0.1.0-SNAPSHOT")

    implementation("software.amazon.awscdk:aws-cdk-lib:2.73.0")
    implementation("software.constructs:constructs:10.1.301")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<ShadowJar> {
    manifest {
        attributes(mapOf(Pair("Main-Class", "uplift.examples.helloweb.Main")))
    }
    mergeServiceFiles()
    minimize()
    dependsOn("build")
}

apply<NativeLambdaPlugin>()

apply<UpliftPlugin>()

tasks.getByName<UpliftTask>(name = "uplift") {
    configure(
        account = "123",
        region = "eu-north-1",
        stack = "uplift-hello-web"
    ).stackWith(
        "uplift.examples.helloweb.HelloWebBuilder"
    )
}.dependsOn("native-lambda", "jar")
