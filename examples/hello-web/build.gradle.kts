allprojects {
    group = "com.github.kjetilv.uplift.examples"
    version = "0.1.1-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
    }

    tasks {
        withType<Test> {
            this.useJUnitPlatform()
        }
    }
}
