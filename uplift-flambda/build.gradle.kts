import com.github.kjetilv.uplift.bld.Native
import com.github.kjetilv.uplift.bld.Native.runCommand

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(project(":uplift-uuid"))

    implementation(project(":uplift-flogs"))
    implementation(project(":uplift-kernel"))
    implementation(project(":uplift-lambda"))
    implementation(project(":uplift-json"))
    implementation(project(":uplift-asynchttp"))

    implementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
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
