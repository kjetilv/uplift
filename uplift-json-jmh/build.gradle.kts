plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    implementation(project(":uplift-json"))
    implementation(project(":uplift-uuid"))
    implementation(project(":uplift-json-ffm"))
    annotationProcessor(project(":uplift-json"))

    implementation("com.github.kjetilv.flopp:flopp-kernel:0.1.0-SNAPSHOT")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")
    implementation("org.openjdk.jmh:jmh-core:1.37")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}
