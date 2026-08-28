pluginManagement {
    includeBuild("..")
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

plugins {
    id("io.github.supermonster003.autojs6-platform-versions")
}

rootProject.name = "sample"
