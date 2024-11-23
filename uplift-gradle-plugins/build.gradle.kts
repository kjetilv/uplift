plugins {
    kotlin("jvm") version "2.0.21"
    `java-gradle-plugin`
    `maven-publish`
}

gradlePlugin {
    website = "https://github.com/kjetilv/uplift"
    vcsUrl = "https://github.com/kjetilv/uplift"
    plugins {
        create("lambda-native") {
            id = "com.github.kjetilv.uplift.plugins.lambda"
            implementationClass = "com.github.kjetilv.uplift.plugins.NativeLambdaPlugin"
            displayName = "Uplift/lambda-native"
            description = "Create a native lambda function"
            tags = listOf("uplift", "lambda", "native")
        }
        create("uplift") {
            id = "com.github.kjetilv.uplift.plugins.uplift"
            implementationClass = "com.github.kjetilv.uplift.plugins.UpliftPlugin"
            displayName = "Uplift/uplift"
            description = "Elevates a native lambda function to the celestial spheres"
            tags = listOf("uplift", "lambda", "native")
        }
    }
}

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.154.1")
    implementation("software.amazon.awssdk:lambda:2.27.11")
    implementation("software.amazon.awssdk:cloudformation:2.27.11")
    implementation("software.amazon.awssdk:auth:2.27.11")
    implementation("software.constructs:constructs:10.3.0")
    implementation("org.antlr:ST4:4.3")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(gradleApi())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

kotlin {
    jvmToolchain(21)
}
