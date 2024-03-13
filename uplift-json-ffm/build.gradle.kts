tasks {
    withType<JavaCompile> {
        options.compilerArgs.add("--enable-preview")
        options.forkOptions.jvmArgs!!.add("--enable-preview")
    }
    withType<Test> {
        jvmArgs("--enable-preview")
        useJUnitPlatform()
    }
    withType<JavaExec> {
        jvmArgs("--enable-preview")
    }
}

dependencies {
    implementation(project(":uplift-uuid"))
    implementation(project(":uplift-json"))

    implementation("com.github.kjetilv.flopp:flopp-kernel:0.1.0-SNAPSHOT")

    testImplementation(project(":uplift-json-samplegen"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
}
