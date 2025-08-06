dependencies {
    implementation(project(":uplift-uuid"))
    implementation(project(":uplift-json"))

    testAnnotationProcessor(project(":uplift-json"))

    testImplementation(project(":uplift-json-samplegen"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")
    testImplementation("org.assertj:assertj-core:3.27.3")
}
