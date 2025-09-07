plugins {
    `java-library`
    `maven-publish`
}

dependencies {

    implementation(project(":uplift-hash"))
    implementation(project(":uplift-json"))
    implementation(project(":uplift-json-gen"))
    implementation(project(":uplift-json-mame"))
    implementation(project(":uplift-util"))
    implementation(project(":uplift-uuid"))

    annotationProcessor(project(":uplift-json-gen"))

    implementation("com.fasterxml.jackson.core:jackson-core:2.20.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
    implementation("org.openjdk.jmh:jmh-core:1.37")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}
