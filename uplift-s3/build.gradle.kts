dependencies {
    implementation(project(":uplift-flogs"))
    implementation(project(":uplift-json"))
    implementation(project(":uplift-kernel"))

    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.26.0")}
