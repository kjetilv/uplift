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
    apply(plugin = "maven-publish")

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }
    }

    apply(plugin = "java")

    JavaVersion.valueOf("VERSION_${property("javaVersion") ?: "17"}")
        .also { javaVersion ->
            configure<JavaPluginExtension> {
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
                withSourcesJar()
            }
        }

    if (project.name != "uplift-gradle-plugins") {
        publishing {
            repositories {
                mavenLocal()
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/kjetilv/uplift")
                    credentials {
                        username = resolveProperty("githubUser", "GITHUB_ACTOR")
                        password = resolveProperty("githubToken", "GITHUB_TOKEN")
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

fun resolveProperty(property: String, variable: String) =
    System.getProperty(property)
        ?: System.getenv(variable)
        ?: project.property(property)?.toString()
        ?: property
