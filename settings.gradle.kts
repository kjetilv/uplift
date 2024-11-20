rootProject.name = "uplift"

pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        maven {
            url = uri("https://maven.pkg.github.com/kjetilv/uplift")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

include("uplift-asynchttp")
include("uplift-cdk")
include("uplift-flambda")
include("uplift-flogs")
include("uplift-gradle-plugins")
include("uplift-json")
include("uplift-json-ffm")
include("uplift-json-samplegen")
include("uplift-kernel")
include("uplift-lambda")
include("uplift-uuid")
include("uplift-s3")

//includeBuild("examples/hello-web")
