import org.autojs.build.platform.PlatformVersionsExtension

// Resolve AGP without applying it. AGP 9 contributes its bundled KGP to this
// classpath, so the assertion below proves that the settings plugin upgrades it
// to the centrally selected version even when no Kotlin plugin is requested.
// zh-CN: 只解析而不应用 AGP. AGP 9 会把捆绑的 KGP 带入此 classpath;
// 下方断言据此验证: 即使未请求 Kotlin 插件, Settings 插件也会将其提升到中央选择版本.
plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version") apply false
}

/**
 * Prints what the settings plugin decided, so the three scenarios (map covers the IDE,
 * map is stale, plain command line) can be checked from the command line.
 *
 * zh-CN: 打印 settings 插件的决策结果, 便于从命令行核对三种场景
 * (映射表覆盖当前 IDE / 映射表滞后 / 纯命令行).
 */
val platformVersions = gradle.extra["platformVersions"] as PlatformVersionsExtension
val rootBuildscriptClasspath = buildscript.configurations.named("classpath")

tasks.register("printPlatformVersions") {
    val platformName = platformVersions.platform.fullName
    val platformVersion = platformVersions.platform.version
    val agpVersion = platformVersions.agpVersion
    val minimumAgpVersion = platformVersions.minimumAgpVersion
    val kotlinVersion = platformVersions.kotlinVersion
    val kspVersion = platformVersions.kspVersion
    val r8Version = platformVersions.r8Version
    val maxJava = platformVersions.maxSupportedJavaVersion
    val info = platformVersions.versionInfo

    doLast {
        val rootBuildscriptKotlin = rootBuildscriptClasspath.get()
            .incoming.resolutionResult.allComponents
            .mapNotNull { component -> component.moduleVersion }
            .firstOrNull { module ->
                module.group == "org.jetbrains.kotlin" && module.name == "kotlin-gradle-plugin"
            }
            ?.version
        check(rootBuildscriptKotlin == kotlinVersion) {
            "Selected Kotlin $kotlinVersion, but the root buildscript resolved KGP " +
                    "${rootBuildscriptKotlin ?: "none"}."
        }

        println("platform=$platformName $platformVersion")
        println("agp=$agpVersion")
        println("minimumAgp=${minimumAgpVersion ?: "none"}")
        println("kotlin=$kotlinVersion")
        println("rootBuildscriptKotlin=$rootBuildscriptKotlin")
        println("ksp=$kspVersion")
        println("r8=${r8Version ?: "bundled with AGP"}")
        println("maxSupportedJavaVersion=$maxJava")
        info.forEach { println("info| $it") }
    }
}
