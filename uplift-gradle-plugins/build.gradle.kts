plugins {
    kotlin("jvm") version "2.2.0"
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
    implementation("software.amazon.awscdk:aws-cdk-lib:2.207.0")
    implementation("software.amazon.awssdk:lambda:2.32.3")
    implementation("software.amazon.awssdk:cloudformation:2.32.3")
    implementation("software.amazon.awssdk:auth:2.32.3")
    implementation("software.constructs:constructs:10.4.2")
    implementation("org.antlr:ST4:4.3.4")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(gradleApi())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
    withSourcesJar()
}

kotlin {
    jvmToolchain(24)
}
