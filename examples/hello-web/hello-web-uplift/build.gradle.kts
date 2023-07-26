import com.github.kjetilv.uplift.plugins.UpliftPingTask
import com.github.kjetilv.uplift.plugins.UpliftTask

plugins {
    java
    id("com.github.kjetilv.uplift.plugins.uplift") version "0.1.1-SNAPSHOT"
}

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.88.0")
    implementation("software.constructs:constructs:10.2.69")
}

tasks.withType<UpliftTask> {

    admonish() // TODO Remove this when you have your properties in order

    // Minimal config, assuming gradle.properties are found
    configure(
        stack = "hello-web-uplift"
    )

// TODO: Either:
//  1. Uncomment and tweak + replace above configure with this one, or
//  2. Remove this block and provide the below settings in a gradle.properties file
//    configure(
//        account = "<your 12-digit AWS account id>"",
//        region = "<your preferred region>",
//        profile = "<AWS profile holding key/secret>",
//        stack = "hello-web-uplift"
//    )

    if (this !is UpliftPingTask) {
        dependsOn(
            ":hello-web-service:native-lambda",
            "jar"
        )
    }
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
        properties.filterValues { it.isBlank() }
            .takeUnless { it.isEmpty() }
            ?.also { missing ->
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
    }
