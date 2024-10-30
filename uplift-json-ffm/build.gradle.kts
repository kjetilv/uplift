repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(project(":uplift-uuid"))
    implementation(project(":uplift-json"))
    implementation("com.github.kjetilv.flopp:flopp-kernel:0.1.0-SNAPSHOT")

    testImplementation(project(":uplift-json-samplegen"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
    testImplementation("org.assertj:assertj-core:3.26.3")
}
