plugins {
    kotlin("jvm") version "1.9.22"
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

dependencies {
implementation("software.amazon.awscdk:aws-cdk-lib:2.124.0")
    implementation("software.amazon.awssdk:lambda:2.23.4")
    implementation("software.amazon.awssdk:cloudformation:2.23.4")
    implementation("software.amazon.awssdk:auth:2.23.4")
    implementation("software.constructs:constructs:10.2.69")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(gradleApi())
    implementation("org.antlr:ST4:4.3.4")
}

kotlin {
    jvmToolchain(21)
}
