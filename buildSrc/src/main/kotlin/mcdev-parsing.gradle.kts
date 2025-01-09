import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    idea
}

val jflex: Configuration by configurations.creating
val jflexSkeleton: Configuration by configurations.creating
val grammarKit: Configuration by configurations.creating

val libs = the<LibrariesForLibs>()
dependencies {
    jflex(libs.jflex.lib)
    jflexSkeleton(libs.jflex.skeleton) {
        artifact {
            extension = "skeleton"
        }
    }
    grammarKit(libs.grammarKit)
}

idea {
    module {
        generatedSourceDirs.add(file("build/gen"))
    }
}
