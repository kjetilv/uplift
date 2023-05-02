rootProject.name = "hello-web"

pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

include("hello-web-service")
include("hello-web-uplift")
