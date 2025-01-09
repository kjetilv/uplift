plugins {
    java
    `maven-publish`
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "jvm-test-suite")
    apply(plugin = "java")

    group = "com.github.kjetilv.uplift"
    version = "0.1.1-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/kjetilv/uplift")
            credentials {
                username = resolveProperty("githubUser", "GITHUB_ACTOR")
                password = resolveProperty("githubToken", "GITHUB_TOKEN")
            }
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(23))
        }
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_23
        targetCompatibility = JavaVersion.VERSION_23
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }

        withType<JavaCompile>().all {
            options.compilerArgs.addAll(listOf("--enable-preview", "--add-modules", "jdk.incubator.vector"))
        }

        withType<Test>().all {
            jvmArgs("--enable-preview", "--add-modules", "jdk.incubator.vector")
        }

        withType<JavaExec>().configureEach {
            jvmArgs("--enable-preview", "--add-modules", "jdk.incubator.vector")
        }
    }

    if (project.name in listOf(
            "uplift-flogs",
            "uplift-uuid",
            "uplift-s3",
            "uplift-lambda",
            "uplift-flambda",
            "uplift-json",
            "uplift-json-ffm",
            "uplift-json-kernel",
            "uplift-gradle-plugins"
        )
    ) {
        logger.info("Configuring ${project.name} for publishing")

        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(23))
                vendor.set(JvmVendorSpec.GRAAL_VM)
            }
            sourceCompatibility = JavaVersion.VERSION_23
            targetCompatibility = JavaVersion.VERSION_23
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
                    suppressAllPomMetadataWarnings()
                    pom {
                        name.set("uplift")
                        description.set("Uplift")
                        url.set("https://github.com/kjetilv/uplift")
                        licenses {
                            license {
                                name.set("GNU General Public License v3.0")
                                url.set("https://raw.githubusercontent.com/kjetilv/uplift/refs/heads/main/LICENSE")
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
        ?: throw IllegalStateException("No variable $property found, no default value provided")
