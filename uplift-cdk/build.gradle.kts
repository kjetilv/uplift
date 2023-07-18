dependencies {
    implementation(project(":uplift-kernel"))
    implementation(project(":uplift-lambda"))

    implementation("software.amazon.awscdk:aws-cdk-lib:2.87.0")
    implementation("software.constructs:constructs:10.2.69")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("org.assertj:assertj-core:3.24.2")
}
