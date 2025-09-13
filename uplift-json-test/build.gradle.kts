dependencies {
    implementation(project(":uplift-uuid"))
    implementation(project(":uplift-json"))
    implementation(project(":uplift-json-anno"))

    annotationProcessor(project(":uplift-json-gen"))

    testImplementation(project(":uplift-json-gen"))
    testImplementation(project(":uplift-json-samplegen"))
}
