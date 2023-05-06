import com.github.kjetilv.uplift.plugins.UpliftTask

plugins {
    java
    id("com.github.kjetilv.uplift.plugins.uplift") version "0.1.0"
}

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.73.0")
    implementation("software.constructs:constructs:10.1.301")
}

tasks.withType<UpliftTask> {

    admonish() // TODO Remove this when you have your properties in order

// // TODO One way to do it: Uncomment and fix, or provide suitable gradle.properties
//    configure(
//        account = "<your 12-digit AWS account id>"",
//        region = "<your preferred region>",
//        profile = "<AWS profile holding key/secret>",
//        stack = hello-web-hello-web-uplift" // This is the not-so-nice default
//    )
//    env(
//        "FOO" to "bar"
//    )
//    stackWith(
//        "uplift.examples.helloweb.HelloWebBuilder"
//    )
    dependsOn(
        ":hello-web-service:native-lambda",
        "jar"
    )
}

fun admonish() =
    listOf("account", "region", "profile").associateWith { prop ->
        try {
            prop.also {
                logger.info("$prop=${project.property(prop)}")
            }
        } catch (e: Exception) {
            "".also {
                logger.error("Missing property $prop, deploy step will fail:\n $e")
            }
        }
    }.also { properties ->
        val missing = properties.filterValues { it.isBlank() }
        if (missing.isEmpty())
            logger.warn("##\n## Great, we have configuration! Now you can remove this nasty method\n##")
        else
            logger.error(
                "\n##\n## MISSING CONFIG FOR UPLIFT\n##\n\nYou need to provide value${
                    if (missing.size > 1) "s" else ""
                } for propert${
                    if (missing.size > 1) "ies" else "y"
                } ${
                    missing.keys.joinToString()
                }!\n\nTo set, either plug them right into this build file, or provide them as gradle properties.\n" +
                        "(E.g. add a gradle.properties file in ${System.getProperty("user.dir")},\n" +
                        "and then, remove this method admonishing you.)"
            )
    }
