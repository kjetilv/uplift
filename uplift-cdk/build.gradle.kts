dependencies {
    implementation(project(":uplift-kernel"))
    implementation(project(":uplift-lambda"))

    implementation("software.amazon.awscdk:aws-cdk-lib:2.207.0")
    implementation("software.constructs:constructs:10.4.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.3")
}
