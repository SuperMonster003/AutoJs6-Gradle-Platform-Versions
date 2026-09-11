pluginManagement {
    includeBuild("../..")
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement { repositories { google(); mavenCentral() } }
rootProject.name = "native-alignment-negative-sample"
