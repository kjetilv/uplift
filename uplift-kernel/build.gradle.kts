dependencies {
    implementation(project(":uplift-flogs"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}
