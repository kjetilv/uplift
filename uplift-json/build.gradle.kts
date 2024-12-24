dependencies {
    implementation(project(":uplift-uuid"))
    implementation("com.github.kjetilv.flopp:flopp-kernel:0.1.0-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.26.3")
}
