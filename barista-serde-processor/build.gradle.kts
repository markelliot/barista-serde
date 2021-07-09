plugins {
    `java-library`
}

dependencies {
    annotationProcessor("com.google.auto.service:auto-service")
    compileOnly("com.google.auto.service:auto-service")

    implementation(project(":barista-serde-annotations"))
    implementation(project(":barista-serde"))
    implementation("com.google.guava:guava")
    implementation("com.squareup:javapoet")

    testAnnotationProcessor(project(":barista-serde-processor"))
    testAnnotationProcessor(project(":barista-serde-annotations"))
    testAnnotationProcessor(project(":barista-serde"))
    testAnnotationProcessor("com.google.guava:guava")
    testAnnotationProcessor("com.squareup:javapoet")

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
