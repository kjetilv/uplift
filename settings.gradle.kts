rootProject.name = "uplift"

pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

include("uplift-flogs")
include("uplift-kernel")
include("uplift-asynchttp")
include("uplift-json")
include("uplift-s3")
include("uplift-flambda")
include("uplift-lambda")
include("uplift-gradle-plugins")
