package org.autojs.build.platform

import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SdkToolingCompatibilityTest {
    private val googleMaven = "https://dl.google.com/dl/android/maven2"

    private fun repositoryUrls(project: org.gradle.api.Project) = project.buildscript.repositories
        .withType(MavenArtifactRepository::class.java)
        .map { it.url.toString().trimEnd('/') }

    @Test
    fun `IDE supported AGP 9_1 gets an XML v4 reader without another AGP dependency`() {
        val project = ProjectBuilder.builder().build()
        SdkToolingCompatibility.configure(project, "9.1.1")

        val dependencies = project.buildscript.configurations.getByName("classpath").dependencies
        assertEquals(listOf("com.android.tools:sdklib:32.2.1"), dependencies.map {
            "${it.group}:${it.name}:${it.version}"
        })
        assertEquals(listOf(googleMaven), repositoryUrls(project))
        val constraints = project.buildscript.configurations.getByName("classpath").dependencyConstraints
        assertEquals(listOf("com.android.tools:common:32.1.1"), constraints.filter {
            it.group == "com.android.tools" && it.name == "common"
        }.map {
            "${it.group}:${it.name}:${it.versionConstraint.strictVersion}"
        })
    }

    @Test
    fun `an existing Google repository is reused`() {
        val project = ProjectBuilder.builder().build()
        project.buildscript.repositories.maven { setUrl("https://maven.google.com/") }
        SdkToolingCompatibility.configure(project, "9.1.0")

        assertEquals(listOf("https://maven.google.com"), repositoryUrls(project))
    }

    @Test
    fun `other AGP lines retain their SDK dependencies`() {
        for (version in listOf("8.13.2", "9.0.1", "9.2.1", "9.4.0", "10.0.0")) {
            val project = ProjectBuilder.builder().build()
            SdkToolingCompatibility.configure(project, version)
            assertTrue(project.buildscript.configurations.getByName("classpath").dependencies.isEmpty(), version)
            assertTrue(project.buildscript.repositories.isEmpty(), version)
        }
    }
}
