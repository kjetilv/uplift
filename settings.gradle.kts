rootProject.name = "uplift"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/kjetilv/uplift")
        }
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        gradlePluginPortal()
    }
}

include("uplift-asynchttp")
include("uplift-cdk")
include("uplift-edamame")
include("uplift-flambda")
include("uplift-flogs")
include("uplift-gradle-plugins")
include("uplift-hash")
include("uplift-json")
include("uplift-json-anno")
include("uplift-json-gen")
include("uplift-json-jmh")
include("uplift-json-mame")
include("uplift-json-samplegen")
include("uplift-json-test")
include("uplift-kernel")
include("uplift-lambda")
include("uplift-s3")
include("uplift-util")
include("uplift-uuid")

include("uplift-hash")
include("uplift-edamame")
include("uplift-json-mame")