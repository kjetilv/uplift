dependencies {
    implementation(project(":uplift-hash"))
    implementation(project(":uplift-json"))
    implementation(project(":uplift-json-anno"))
    implementation(project(":uplift-json-gen"))
    implementation(project(":uplift-util"))

    annotationProcessor(project(":uplift-json-gen"))

    testImplementation(project(":uplift-json-mame"))
    testImplementation(project(":uplift-synchttp"))
    testImplementation(project(":uplift-hash"))
}
