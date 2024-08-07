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
    }
}

includeBuild("uplift-gradle-plugins")

include("uplift-flogs")
include("uplift-uuid")
include("uplift-kernel")
include("uplift-asynchttp")
include("uplift-json")
//include("uplift-json-ffm")
include("uplift-json-samplegen")
include("uplift-s3")
include("uplift-flambda")
include("uplift-lambda")
include("uplift-cdk")

