package org.autojs.build.platform

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository

/**
 * AGP 9.1 is the supported line in IntelliJ IDEA 2026.2, but its SDK parser predates
 * repository XML v4. Update the standalone SDK reader without raising the IDE's AGP
 * ceiling. Keep common on the selected AGP version: it also owns com.android.Version,
 * which AGP uses to construct its AAPT2 coordinates. Newer AGP lines already supply a
 * compatible reader and retain their own versions.
 *
 * zh-CN: IDEA 2026.2 支持 AGP 9.1, 但其 SDK 解析库尚不支持 repository XML v4.
 * 仅更新 SDK 读取库, 保持 common 中的 AGP/AAPT2 版本身份, 不提高 IDE 的 AGP 上限,
 * 不改写本地 SDK 元数据.
 */
internal object SdkToolingCompatibility {
    private const val SDK_READER = "com.android.tools:sdklib:32.2.1"

    fun configure(project: Project, agpVersion: String) {
        if (!agpVersion.startsWith("9.1.")) return
        val repositories = project.buildscript.repositories
        if (repositories.withType(MavenArtifactRepository::class.java).none {
                it.url.toString().trimEnd('/') in setOf(
                    "https://dl.google.com/dl/android/maven2",
                    "https://maven.google.com",
                )
            }) {
            repositories.google()
        }
        project.buildscript.dependencies.add("classpath", SDK_READER)
        val commonVersion = "32.${agpVersion.substringAfter('.')}"
        project.buildscript.dependencies.constraints.add("classpath", "com.android.tools:common:$commonVersion") {
            version { strictly(commonVersion) }
            because("com.android.Version must match AGP when resolving AAPT2")
        }
    }
}
