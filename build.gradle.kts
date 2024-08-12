plugins {
    java
    `maven-publish`
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "java")

    group = "com.github.kjetilv.uplift"
    version = "0.1.1-SNAPSHOT"

    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(22))
        }
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }
    tasks {
        withType<JavaCompile> {
        }
        withType<Test> {
            useJUnitPlatform()
        }
        withType<JavaExec> {
        }
    }

    if (project.name != "uplift-gradle-plugins") {
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(22))
                vendor.set(JvmVendorSpec.GRAAL_VM)
            }
            sourceCompatibility = JavaVersion.VERSION_22
            targetCompatibility = JavaVersion.VERSION_22
            withSourcesJar()
        }

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

fun resolveProperty(property: String, variable: String? = null, defValue: String? = null) =
    System.getProperty(property)
        ?: variable?.let { System.getenv(it) }
        ?: project.takeIf { it.hasProperty(property) }?.property(property)?.toString()
        ?: defValue
        ?: "$\\{$property}"
