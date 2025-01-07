dependencies {
    implementation(project(":uplift-uuid"))
    implementation(project(":uplift-json"))
    implementation("com.github.kjetilv.flopp:flopp-kernel:0.1.0-SNAPSHOT")
    annotationProcessor(project(":uplift-json"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.26.3")
}
