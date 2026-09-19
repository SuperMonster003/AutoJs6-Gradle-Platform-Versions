pluginManagement {
    includeBuild("../..")
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}

plugins { id("io.github.supermonster003.autojs6-platform-versions") }

dependencyResolutionManagement { repositories { google(); mavenCentral() } }
rootProject.name = "sdk-xml-compatibility-sample"
