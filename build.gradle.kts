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
            languageVersion.set(JavaLanguageVersion.of(24))
        }
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
        testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")
        testImplementation("org.assertj:assertj-core:3.27.4")
    }

    if (project.name in listOf(
            "edamame",
            "flambda",
            "flogs",
            "gradle-plugins",
            "hash",
            "json",
            "json-gen",
            "json-ffm",
            "json-kernel",
            "json-mame",
            "lambda",
            "s3",
            "uuid",
        ).map { it -> "uplift-$it" }
    ) {
        logger.info("Configuring ${project.name} for publishing")

        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(24))
                nativeImageCapable = true
            }
            sourceCompatibility = JavaVersion.VERSION_24
            targetCompatibility = JavaVersion.VERSION_24
            withSourcesJar()
        }
    }

    if (project.name in listOf(
            "edamame",
            "flambda",
            "flogs",
            "hash",
            "json",
            "json-gen",
            "json-ffm",
            "json-kernel",
            "json-mame",
            "lambda",
            "s3",
            "uuid",
        ).map { it -> "uplift-$it" }
    ) {
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
                        name = "uplift"
                        description = "Uplift"
                        url = "https://github.com/kjetilv/uplift"
                        licenses {
                            license {
                                name = "GNU General Public License v3.0"
                                url = "https://raw.githubusercontent.com/kjetilv/uplift/refs/heads/main/LICENSE"
                            }
                        }
                        scm {
                            connection = "scm:git:https://github.com/kjetilv/uplift"
                            developerConnection = "scm:git:https://github.com/kjetilv/uplift"
                            url = "https://github.com/kjetilv/uplift"
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
        ?: variable?.let(System::getenv)
        ?: project.property(property)?.toString()
        ?: defValue
        ?: throw IllegalStateException("No variable $property found, no default value provided")
