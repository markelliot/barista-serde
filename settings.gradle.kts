rootProject.name = "barista-serde-root"

include("barista-serde-annotations")
include("barista-serde-benchmarks")
include("barista-serde-json")
include("barista-serde-parsec")
include("barista-serde-processor")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
