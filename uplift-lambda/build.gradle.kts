dependencies {
    implementation(project(":uplift-flogs"))
    implementation(project(":uplift-json"))
    implementation(project(":uplift-json-gen"))
    implementation(project(":uplift-kernel"))
    implementation(project(":uplift-s3"))
    implementation(project(":uplift-util"))
    implementation(project(":uplift-uuid"))

    annotationProcessor(project(":uplift-json-gen"))
}
