//import com.github.kjetilv.uplift.bld.Native
//import com.github.kjetilv.uplift.bld.Native.runCommand

dependencies {
    implementation(project(":uplift-uuid"))

    implementation(project(":uplift-flogs"))
    implementation(project(":uplift-kernel"))
    implementation(project(":uplift-lambda"))
    implementation(project(":uplift-json"))
    implementation(project(":uplift-asynchttp"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.13.4")

    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.3")
}

tasks.register<Copy>("copy-libs") {
    from(configurations.compileOnly)
    into("build/libs")
}

//tasks.register<Task>("native-image")
//    .configure {
//        dependsOn(
//            tasks.named("copy-libs").get(),
//            tasks.named("jar").get()
//        )
//        doLast {
//            project.runCommand(
//                command = Native.image(
//                    "flopp-$version.jar",
//                    "com.github.kjetilv.flopp.lc.Lc",
//                    "lc",
//                    javaToolchains
//                )
//            )
//        }
//    }
