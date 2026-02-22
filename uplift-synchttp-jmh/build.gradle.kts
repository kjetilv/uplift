plugins {
    `java-library`
    `maven-publish`
}

dependencies {

    implementation(project(":uplift-synchttp"))
    implementation(project(":uplift-util"))
    implementation(project(":uplift-hash"))
    implementation(project(":uplift-flogs"))
    implementation(project(":uplift-kernel"))

    implementation("io.netty:netty-all:4.2.10.Final")

    implementation("org.openjdk.jmh:jmh-core:1.37")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

tasks.register<Jar>("jmhJar") {
    archiveClassifier.set("jmh")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "org.openjdk.jmh.Main"
    }
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}

tasks.register<JavaExec>("jmh") {
    dependsOn(tasks.named("compileJava"))
    mainClass.set("org.openjdk.jmh.Main")
    classpath = sourceSets.main.get().runtimeClasspath
    jvmArgs("--enable-preview", "--add-modules", "jdk.incubator.vector")
}
