dependencies {
    implementation(project(":uplift-hash"))
    implementation(project(":uplift-util"))

    testImplementation("org.slf4j:slf4j-api:2.0.17")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.18")

    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
