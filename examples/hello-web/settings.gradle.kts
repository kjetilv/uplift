rootProject.name = "hello-web"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

include("hello-web-service")
include("hello-web-uplift")
