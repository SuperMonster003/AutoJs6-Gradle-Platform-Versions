package org.autojs.build.platform

import java.io.File
import java.util.Properties

/**
 * Loads the compatibility data that drives the version decisions.
 *
 * Each dataset is looked up in the consuming project first (`gradle/data/<name>`),
 * falling back to the copy bundled inside this plugin's jar. The local path remains
 * only for legacy compatibility and temporary diagnostics. Official AutoJs6 consumers
 * must use the bundled resources and ship compatibility fixes in a new immutable
 * central-plugin release instead of committing local copies.
 *
 * zh-CN: 加载驱动版本决策的兼容性数据. 每份数据优先读取消费端项目的 `gradle/data/<name>`,
 * 缺失时回退到本插件 jar 内置的副本. 本地路径仅为旧版兼容和临时诊断保留; AutoJs6
 * 官方消费仓必须使用内置资源, 兼容性修复必须随新的不可变中央插件版本发布, 不得提交
 * 本地副本.
 */
class DataSource(private val localDataDir: File?) {

    /** Reads a `.properties` dataset, e.g. "agp-gradle-compat". */
    fun props(name: String): Map<String, String> {
        val text = read("$name.properties")
            ?: throw IllegalStateException("Missing dataset: $name.properties")
        @Suppress("UNCHECKED_CAST")
        return Properties().apply { load(text.reader()) } as Map<String, String>
    }

    /** Reads a `.list` dataset, e.g. "agp-releases". Blank and commented lines are dropped. */
    fun list(name: String): List<String> {
        val text = read("$name.list")
            ?: throw IllegalStateException("Missing dataset: $name.list")
        return text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .toList()
    }

    /** Tells whether a dataset is provided by the consuming project rather than the plugin jar. */
    fun isOverriddenLocally(fileName: String): Boolean = localFile(fileName)?.isFile == true

    private fun localFile(fileName: String): File? = localDataDir?.resolve(fileName)

    private fun read(fileName: String): String? {
        localFile(fileName)?.takeIf { it.isFile }?.let { return it.readText() }
        return javaClass.getResourceAsStream("$BUNDLED_RESOURCE_DIR/$fileName")?.use {
            it.readBytes().toString(Charsets.UTF_8)
        }
    }

    companion object {
        const val BUNDLED_RESOURCE_DIR = "/org/autojs/build/platform/data"
    }

}
