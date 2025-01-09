import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.gson)
    implementation(libs.kotlin.plugin)
    implementation(libs.intellij.plugin)
    implementation(libs.licenser.plugin)
    implementation(libs.changelog.plugin)
}
