import com.github.kjetilv.uplift.plugins.UpliftPlugin
import com.github.kjetilv.uplift.plugins.UpliftTask

plugins {
    java
    id("com.github.kjetilv.uplift.plugins.uplift") version "0.1.0-SNAPSHOT"
}

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.73.0")
    implementation("software.constructs:constructs:10.1.301")
}

apply<UpliftPlugin>()

tasks.withType<UpliftTask> {
// Uncomment and fix, or provide suitable gradle.properties
//    configure(
//        account = "...",
//        region = "mars-east-1",
//        profile = "default",
//        stack = "mystack"
//    )
//    env(
//        "FOO" to "bar"
//    )
//    stackWith(
//        "uplift.examples.helloweb.HelloWebBuilder"
//    )
    dependsOn(
        ":hello-web-service:native-lambda",
        "jar"
    )
}

