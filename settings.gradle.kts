plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
}

rootProject.name = "MinecraftDev"
include("obfuscation-explorer")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("mixin-test-data")

startParameter.warningMode = WarningMode.All
