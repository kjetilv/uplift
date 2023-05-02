plugins {
    `maven-publish`
}

allprojects {
    group = "com.github.kjetilv.uplift"
    version = "0.1.0-SNAPSHOT"

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

subprojects {
    val sub = project.name != "uplift-gradle-plugins"
    apply(plugin = "maven-publish")

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }
    }

    apply(plugin = "java")

    JavaVersion.valueOf("VERSION_${property("javaVersion")}").also { javaVersion ->
        configure<JavaPluginExtension> {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
            withSourcesJar()
        }
    }

    if (sub) {
        publishing {
            repositories {
                mavenLocal()
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/kjetilv/uplift")
                    credentials {
                        username = resolveUsername()
                        password = resolveToken()
                    }
                }
            }
            publications {
                register<MavenPublication>("upliftPublication") {
                    pom {
                        name.set("uplift")
                        description.set("Uplift")
                        url.set("https://github.com/kjetilv/uplift")

                        licenses {
                            license {
                                name.set("GNU General Public License v3.0")
                                url.set("https://github.com/kjetilv/uplift/blob/main/LICENSE")
                            }
                        }

                        scm {
                            connection.set("scm:git:https://github.com/kjetilv/uplift")
                            developerConnection.set("scm:git:https://github.com/kjetilv/uplift")
                            url.set("https://github.com/kjetilv/uplift")
                        }
                    }
                    from(components["java"])
                }
            }
        }
    }
}

fun resolveUsername() = System.getenv("GITHUB_ACTOR") ?: read(".github_user")

fun resolveToken() = System.getenv("GITHUB_TOKEN") ?: read(".github_token")

fun read(file: String): String =
    project.rootDir.listFiles()?.firstOrNull {
        it.name.equals(file)
    }?.readLines()?.firstOrNull() ?: "No file $file found"
