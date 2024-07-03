plugins {
    java
    id("com.github.kjetilv.uplift.plugins.lambda") version "0.1.1-SNAPSHOT"
}

dependencies {
    implementation("com.github.kjetilv.uplift:uplift-kernel:0.1.1-SNAPSHOT")
    implementation("com.github.kjetilv.uplift:uplift-lambda:0.1.1-SNAPSHOT")

    testImplementation("com.github.kjetilv.uplift:uplift-flambda:0.1.1-SNAPSHOT")
    testImplementation("com.github.kjetilv.uplift:uplift-asynchttp:0.1.1-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.3")
    testImplementation("org.assertj:assertj-core:3.26.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
//        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

