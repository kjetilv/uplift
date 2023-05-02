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

tasks.getByName<UpliftTask>(name = "uplift") {
    configure(
        account = "${project.property("account")}",
        region = "${project.property("region")}",
        profile = "${project.property("profile") ?: "default"}",
        stack = "uplift-hello-web"
    ).stackWith(
        "uplift.examples.helloweb.HelloWebBuilder"
    )
}.dependsOn(
    ":hello-web-service:native-lambda",
    "jar")

