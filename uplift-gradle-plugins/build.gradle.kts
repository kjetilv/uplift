plugins {
    kotlin("jvm") version "2.0.21"
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

publishing {
    publications.withType<MavenPublication> {
        suppressAllPomMetadataWarnings()
    }
}

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.154.1")
    implementation("software.amazon.awssdk:lambda:2.27.11")
    implementation("software.amazon.awssdk:cloudformation:2.27.11")
    implementation("software.amazon.awssdk:auth:2.27.11")
    implementation("software.constructs:constructs:10.3.0")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(gradleApi())
    implementation("org.antlr:ST4:4.3")
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
