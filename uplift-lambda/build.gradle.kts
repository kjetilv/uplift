dependencies {
    implementation(project(":uplift-flogs"))
    implementation(project(":uplift-json"))
    implementation(project(":uplift-s3"))
    implementation(project(":uplift-uuid"))
    implementation(project(":uplift-kernel"))
    annotationProcessor(project(":uplift-json"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
}
