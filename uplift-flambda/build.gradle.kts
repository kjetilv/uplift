//import com.github.kjetilv.uplift.bld.Native
//import com.github.kjetilv.uplift.bld.Native.runCommand

dependencies {
    implementation(project(":uplift-asynchttp"))
    implementation(project(":uplift-flogs"))
    implementation(project(":uplift-json"))
    implementation(project(":uplift-kernel"))
    implementation(project(":uplift-lambda"))
    implementation(project(":uplift-util"))
    implementation(project(":uplift-uuid"))

    implementation("org.junit.jupiter:junit-jupiter-api:6.0.0-RC3")
}

tasks.register<Copy>("copy-libs") {
    from(configurations.compileOnly)
    into("build/libs")
}
