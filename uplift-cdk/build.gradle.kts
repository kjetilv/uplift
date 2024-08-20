dependencies {
    implementation(project(":uplift-kernel"))
    implementation(project(":uplift-lambda"))

    implementation("software.amazon.awscdk:aws-cdk-lib:2.151.0")
    implementation("software.constructs:constructs:10.3.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
    testImplementation("org.assertj:assertj-core:3.26.3")
}
