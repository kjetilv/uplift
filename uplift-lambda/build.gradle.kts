dependencies {
    implementation(project(":uplift-flogs"))
    implementation(project(":uplift-json"))
    implementation(project(":uplift-s3"))
    implementation(project(":uplift-uuid"))
    implementation(project(":uplift-kernel"))
    annotationProcessor(project(":uplift-json"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.3")
}
