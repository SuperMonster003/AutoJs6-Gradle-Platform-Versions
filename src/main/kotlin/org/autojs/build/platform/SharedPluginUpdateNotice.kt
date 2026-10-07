package org.autojs.build.platform

import java.io.File
import java.util.Properties

/**
 * Tells a consumer when a newer shared plugin version exists in the maintainer's local checkout.
 *
 * Consumers pin `io.github.supermonster003.autojs6-platform-versions` to an exact version and
 * nothing reminds them when a newer one has been released, so bumps lag behind. The sibling
 * checkout (`../AutoJs6-Gradle-Platform-Versions`, or the directory named by the
 * `autojs.buildPlugins.localCheckout` project property) carries the next version in its
 * `version.properties`; when it is newer than the version in use, one footer line is added to
 * the version information block. The bump itself stays an explicit step:
 * `.python/bump_consumers.py` in that checkout rewrites every sibling's settings script.
 *
 * zh-CN: 当相邻的插件仓检出 (或 `autojs.buildPlugins.localCheckout` 指定的目录) 的
 * `version.properties` 比当前使用的版本新时, 在版本信息块末尾追加一行提示; 实际升级仍由
 * 该仓的 `.python/bump_consumers.py` 显式完成.
 */
object SharedPluginUpdateNotice {

    const val LOCAL_CHECKOUT_PROPERTY = "autojs.buildPlugins.localCheckout"
    const val SIBLING_DIRECTORY_NAME = "AutoJs6-Gradle-Platform-Versions"
    private const val VERSION_RESOURCE = "/org/autojs/build/platform/plugin-version.properties"

    /** The version of the plugin jar in use, generated into the jar at build time. */
    fun currentVersion(): String? = runCatching {
        SharedPluginUpdateNotice::class.java.getResourceAsStream(VERSION_RESOURCE)?.use { stream ->
            Properties().apply { load(stream) }.getProperty("VERSION_NAME")?.trim()?.takeIf { it.isNotEmpty() }
        }
    }.getOrNull()

    fun detect(
        rootDir: File,
        projectProperties: Map<String, String>,
        current: String? = currentVersion(),
    ): String? {
        val inUse = current?.takeIf { it.isNotBlank() && !it.endsWith("-SNAPSHOT") } ?: return null
        val checkout = projectProperties[LOCAL_CHECKOUT_PROPERTY]?.takeIf { it.isNotBlank() }?.let(::File)
            ?: rootDir.absoluteFile.parentFile?.resolve(SIBLING_DIRECTORY_NAME)
            ?: return null
        val local = checkout.resolve("version.properties").takeIf { it.isFile }?.let { file ->
            runCatching { Properties().apply { file.inputStream().use(::load) }.getProperty("VERSION_NAME")?.trim() }.getOrNull()
        }?.takeIf { it.isNotEmpty() && !it.endsWith("-SNAPSHOT") } ?: return null
        if (compareVersions(local, inUse) <= 0) return null
        return "Shared plugins: $inUse in use, $local available in ${checkout.path} (publish it, then run .python/bump_consumers.py there)"
    }

    /** Numeric dot-separated comparison; a shorter version is padded with zeros. */
    fun compareVersions(left: String, right: String): Int {
        val a = left.split('.').map { it.toIntOrNull() ?: 0 }
        val b = right.split('.').map { it.toIntOrNull() ?: 0 }
        val size = maxOf(a.size, b.size)
        for (index in 0 until size) {
            val delta = (a.getOrNull(index) ?: 0).compareTo(b.getOrNull(index) ?: 0)
            if (delta != 0) return delta
        }
        return 0
    }
}
