import org.autojs.build.platform.PlatformVersionsExtension

/**
 * Prints what the settings plugin decided, so the three scenarios (map covers the IDE,
 * map is stale, plain command line) can be checked from the command line.
 *
 * zh-CN: 打印 settings 插件的决策结果, 便于从命令行核对三种场景
 * (映射表覆盖当前 IDE / 映射表滞后 / 纯命令行).
 */
val platformVersions = gradle.extra["platformVersions"] as PlatformVersionsExtension

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
        println("platform=$platformName $platformVersion")
        println("agp=$agpVersion")
        println("minimumAgp=${minimumAgpVersion ?: "none"}")
        println("kotlin=$kotlinVersion")
        println("ksp=$kspVersion")
        println("r8=${r8Version ?: "bundled with AGP"}")
        println("maxSupportedJavaVersion=$maxJava")
        info.forEach { println("info| $it") }
    }
}
