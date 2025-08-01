subprojects {

    group = "com.github.kjetilv.uplift.examples"
    version = "0.1.1-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/kjetilv/uplift")
            credentials {
                username = "githubUser".prop()
                password = "githubToken".prop()
            }
        }
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }
    }
}

fun String.prop() =
    project.takeIf { it.hasProperty(this) }?.property(this)?.toString()
