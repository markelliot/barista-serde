pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "barista-serde-root"

include("barista-serde-annotations")
include("barista-serde-benchmarks")
include("barista-serde-json")
include("barista-serde-parsec")
include("barista-serde-processor")
