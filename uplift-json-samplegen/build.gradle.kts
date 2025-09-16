dependencies {
    implementation(project(":uplift-uuid"))
    implementation(project(":uplift-json"))
    implementation(project(":uplift-json-anno"))
    implementation(project(":uplift-json-gen"))

    annotationProcessor(project(":uplift-json-gen"))

    testImplementation(project(":uplift-json-mame"))
    testImplementation(project(":uplift-hash"))
}
