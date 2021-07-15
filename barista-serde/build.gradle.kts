plugins {
    `java-library`
}

dependencies {
    implementation("io.github.markelliot.result:result")

    testImplementation("com.google.guava:guava")
    testImplementation(platform("org.junit:junit-bom"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-core")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
