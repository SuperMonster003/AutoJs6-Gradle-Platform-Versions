plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version")
    id("io.github.supermonster003.autojs6-native-alignment")
}

android {
    namespace = "org.autojs.test.sdkxml"
    compileSdk = 37
    // AGP 9.1.1 defaults to build-tools 36.0.0; CI only provisions 37.0.0.
    buildToolsVersion = "37.0.0"
    defaultConfig { applicationId = namespace; minSdk = 24; targetSdk = 37 }
}

val toolingClasspath = buildscript.configurations.named("classpath")
val selectedAgp = System.getProperty("gradle.agp.version")
tasks.register("verifyToolingCompatibility") {
    dependsOn("assembleDebug", "assembleDebugUnitTest")
    doLast {
        val modules = toolingClasspath.get().resolvedConfiguration.resolvedArtifacts
            .associate { "${it.moduleVersion.id.group}:${it.name}" to it.moduleVersion.id.version }
        check(modules["com.android.tools:sdklib"] == "32.2.1")
        check(modules["com.android.tools:common"] == "32.1.1")
        check(com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION == selectedAgp)
        val unitTestAssembly = tasks.named("assembleDebugUnitTest").get()
        check(unitTestAssembly.finalizedBy.getDependencies(unitTestAssembly).none {
            it.name.endsWith("NativePageAlignment")
        })
    }
}
