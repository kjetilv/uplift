rootProject.name = "hello-web"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/kjetilv/uplift")
        }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.github.kjetilv.uplift.plugins") {
                useModule("com.github.kjetilv.uplift:uplift-gradle-plugins:${requested.version}")
            }
        }
    }
}

include("hello-web-service")
include("hello-web-uplift")
