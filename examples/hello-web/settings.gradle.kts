rootProject.name = "hello-web"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

include("hello-web-service")
include("hello-web-uplift")
