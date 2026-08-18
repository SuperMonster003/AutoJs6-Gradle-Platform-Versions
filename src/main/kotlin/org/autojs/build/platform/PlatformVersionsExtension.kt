package org.autojs.build.platform

/**
 * The decisions this plugin made, readable from the consuming settings script.
 *
 * ```kotlin
 * plugins { id("org.autojs.build.platform-versions") version "1.0.0" }
 * println(extensions.getByType<PlatformVersionsExtension>().agpVersion)
 * ```
 *
 * zh-CN: 本插件做出的版本决策, 可在消费端 settings 脚本中读取.
 */
class PlatformVersionsExtension(

    /** The detected build host, e.g. IntelliJ IDEA 2026.2. */
    val platform: Platform,

    /** The decided Android Gradle plugin version. */
    val agpVersion: String,

    /** The decided Kotlin Gradle plugin version. */
    val kotlinVersion: String,

    /** The highest Java version the running Gradle fully supports. */
    val maxSupportedJavaVersion: Int,

    /** The Java version pinned in version.properties, or null when left on NONE. */
    val overriddenJavaVersion: Int?,

    /** Console lines describing how each version was decided. */
    val versionInfo: List<String>,
) {

    /** The buildscript classpath notation for AGP, ready to be passed to `classpath(...)`. */
    val agpClasspathNotation: String get() = "com.android.tools.build:gradle:$agpVersion"

    /** The buildscript classpath notation for the Kotlin Gradle plugin. */
    val kotlinClasspathNotation: String get() = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"

}
