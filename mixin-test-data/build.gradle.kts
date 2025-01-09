plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.debugOptions.debugLevel = "vars"
    options.release.set(11)
}
