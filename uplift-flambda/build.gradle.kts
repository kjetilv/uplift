dependencies {
    implementation(project(":uplift-flogs"))

    implementation(project(":uplift-kernel"))
    implementation(project(":uplift-lambda"))
    implementation(project(":uplift-asynchttp"))

    implementation("com.fasterxml.jackson.core:jackson-core:2.14.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")
    implementation("org.junit.jupiter:junit-jupiter-api:5.9.2")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
}
