dependencies {
    implementation(project(":uplift-uuid"))
    implementation(project(":uplift-json"))
    implementation(project(":uplift-hash"))
    implementation(project(":uplift-edamame"))
    implementation("com.github.kjetilv.flopp:flopp-kernel:0.1.0-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.3")
    testImplementation("org.assertj:assertj-core:3.27.3")
}
