
dependencies {
    implementation(project(":uplift-hash"))
    implementation(project(":uplift-util"))

    testImplementation("org.slf4j:slf4j-api:2.0.17")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.18")

    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.13.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.3")
}

tasks.withType<JavaCompile>().all {
    options.compilerArgs.addAll(sequenceOf("--enable-preview", "--add-modules", "jdk.incubator.vector"))
}
