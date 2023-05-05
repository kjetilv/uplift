import com.github.kjetilv.uplift.plugins.UpliftPlugin
import com.github.kjetilv.uplift.plugins.UpliftTask

plugins {
    java
    id("com.github.kjetilv.uplift.plugins.uplift") version "0.1.0-SNAPSHOT"
}

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.73.0")
    implementation("software.constructs:constructs:10.1.301")
}

apply<UpliftPlugin>()

tasks.withType<UpliftTask> {

    admonish() // TODO Remove this when you have your properties in order

// // TODO One way to do it: Uncomment and fix, or provide suitable gradle.properties
//    configure(
//        account = "<your 12-digit AWS account id>"",
//        region = "<your preferred region>",
//        profile = "default", // This is the default
//        stack = hello-web-hello-web-uplift" // This is the silly default
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

fun admonish() {
    listOf("account", "region").associateWith { prop ->
        try {
            project.property(prop).toString()
        } catch (e: Exception) {
            "".also {
                logger.error("Missing property $prop, deploy step will fail:\n $e")
            }
        }
    }.also { map ->
        val missing = map.entries.filter { it.value.isBlank() }
            .takeIf { it.isNotEmpty() }
            ?.let { list ->
                logger.error("\nMISSING SETUP FOR UPLIFT\n\nYou need to provide value${
                    if (list.size > 1) "s" else ""
                } for propert${
                    if (list.size > 1) "ies" else "y"
                } ${
                    list.joinToString { "`${it.key}`" }
                }!\n\nTo set, either plug them right into this build file, or set them as gradle properties.\n" +
                        "(E.g. add a gradle.properties file in ${System.getProperty("user.dir")})\n" +
                        "(And then, remove the method admonishing you like this.)")
            }
        if (missing == null) {
            logger.info("Great! Now you can remove this nasty method")
        }
    }
}
