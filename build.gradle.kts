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
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()
    }

    publishing {
        repositories {
            mavenLocal()
        }
        publications {
            if (project.name != "uplift-gradle-plugins") {
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
