import com.github.kjetilv.uplift.plugins.NativeLambdaPlugin
import com.github.kjetilv.uplift.plugins.NativeLamdbdaTask

plugins {
    java
    id("com.github.kjetilv.uplift.plugins.lambda") version "0.1.1-SNAPSHOT"
}

dependencies {
    implementation("com.github.kjetilv.uplift:uplift-kernel:0.1.1-SNAPSHOT")
    implementation("com.github.kjetilv.uplift:uplift-lambda:0.1.1-SNAPSHOT")

    testImplementation("com.github.kjetilv.uplift:uplift-flambda:0.1.1-SNAPSHOT")
    testImplementation("com.github.kjetilv.uplift:uplift-asynchttp:0.1.1-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:6.0.1"))

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.assertj:assertj-core:3.27.4")
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

apply<NativeLambdaPlugin>()

tasks.test {
    useJUnitPlatform()
}

tasks.withType<NativeLamdbdaTask> { main = "helloweb" }
