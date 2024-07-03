dependencies {
    implementation(project(":uplift-uuid"))
    implementation(project(":uplift-json"))
    annotationProcessor(project(":uplift-json"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.3")
    testImplementation("org.assertj:assertj-core:3.26.0")
}
