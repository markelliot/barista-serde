plugins {
    `java-library`
}

dependencies {
    annotationProcessor("com.google.auto.service:auto-service")
    compileOnly("com.google.auto.service:auto-service")

    implementation(project(":barista-serde-annotations"))
    implementation(project(":barista-serde"))

    testAnnotationProcessor(project(":barista-serde-processor"))
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
