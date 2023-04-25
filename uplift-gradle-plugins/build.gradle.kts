plugins {
    kotlin("jvm") version "1.8.20"
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.github.kjetilv.uplift"
version = "0.1.0-SNAPSHOT"

gradlePlugin {
    plugins {
        create("lambda-native") {
            id = "com.github.kjetilv.uplift.plugins.native"
            implementationClass = "com.github.kjetilv.uplift.plugins.NativeLambdaPlugin"
        }
        create("uplift") {
            id = "com.github.kjetilv.uplift.plugins.uplift"
            implementationClass = "com.github.kjetilv.uplift.plugins.UpliftPlugin"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.73.0")
    implementation("software.constructs:constructs:10.1.301")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(gradleApi())
    implementation("org.antlr:ST4:4.3.4")
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = "17"
}
