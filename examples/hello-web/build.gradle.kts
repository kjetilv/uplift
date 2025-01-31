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
            this.useJUnitPlatform()
        }
    }
}

fun String.prop() =
    project.takeIf { project.hasProperty(this) }?.property(this)?.toString()
