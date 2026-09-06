plugins {
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
    // Build logic and templates, shared with the Maven plugin. Resolved from mavenLocal,
    // so `mvn -pl uplift-plugin-core install` must run before this module is built.
    // Temporary: this module goes away at the end of the Maven migration.
    implementation("com.github.kjetilv.uplift:uplift-plugin-core:0.1.1-SNAPSHOT")

    implementation(gradleApi())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        nativeImageCapable = true
    }
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
}
