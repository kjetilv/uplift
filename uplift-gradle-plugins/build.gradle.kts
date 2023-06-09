plugins {
    kotlin("jvm") version "1.8.21"
    `java-gradle-plugin`
}

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

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.86.0")
    implementation("software.amazon.awssdk:lambda:2.20.97")
    implementation("software.amazon.awssdk:cloudformation:2.20.97")
    implementation("software.amazon.awssdk:auth:2.20.97")
    implementation("software.constructs:constructs:10.2.65")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(gradleApi())
    implementation("org.antlr:ST4:4.3.4")
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = project.takeIf { it.hasProperty("javaVersion") }
        ?.property("javaVersion")
        ?.toString()
        ?: "17"
}
