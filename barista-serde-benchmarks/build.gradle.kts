import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
}

// disable final class requirements becuase of jmh generated code
tasks.withType<JavaCompile> {
    options.errorprone.disable("Nopen")
}

dependencies {
    implementation(project(":barista-serde-json"))
    implementation(project(":barista-serde-annotations"))

    annotationProcessor(project(":barista-serde-processor"))
    // TODO(markelliot): figure out why the rest of these are necessary for intellij
    // (for now, keep in sync with implementation deps)
    annotationProcessor(project(":barista-serde-annotations"))
    annotationProcessor(project(":barista-serde-json"))
    annotationProcessor("com.google.googlejavaformat:google-java-format")
    annotationProcessor("com.google.guava:guava")
    annotationProcessor("com.squareup:javapoet")

    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-guava")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner")
    implementation("com.google.guava:guava")

    implementation("org.openjdk.jmh:jmh-core")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess")

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
