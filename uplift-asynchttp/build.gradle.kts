dependencies {
    implementation(project(":uplift-util"))
    implementation(project(":uplift-hash"))
    implementation(project(":uplift-flogs"))
    implementation(project(":uplift-kernel"))
}

tasks.withType<JavaCompile>().all {
    options.compilerArgs.addAll(listOf("--add-modules", "jdk.incubator.vector"))
}

tasks.withType<Test>().all {
    options.compilerArgs.addAll(listOf("--add-modules", "jdk.incubator.vector"))
}

