plugins {
    kotlin("jvm") version "2.0.0"
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("lambda-native") {
            id = "com.github.kjetilv.uplift.plugins.lambda"
            implementationClass = "com.github.kjetilv.uplift.plugins.NativeLambdaPlugin"
        }
        create("uplift") {
            id = "com.github.kjetilv.uplift.plugins.uplift"
            implementationClass = "com.github.kjetilv.uplift.plugins.UpliftPlugin"
        }
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.147.2")
    implementation("software.amazon.awssdk:lambda:2.25.27")
    implementation("software.amazon.awssdk:cloudformation:2.25.52")
    implementation("software.amazon.awssdk:auth:2.25.27")
    implementation("software.constructs:constructs:10.2.69")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(gradleApi())
    implementation("org.antlr:ST4:4.3")
}

kotlin {
    jvmToolchain(21)
}
