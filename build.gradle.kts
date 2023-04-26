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
            }
            publications {
                create<MavenPublication>("mavenJava") {
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
