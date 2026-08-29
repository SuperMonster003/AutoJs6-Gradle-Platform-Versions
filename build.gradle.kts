import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.plugin.compatibility.compatibility
import org.gradle.plugins.signing.SigningExtension
import java.util.Properties

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.1.1"
}

val projectUrl = "https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions"
val projectVcsUrl = "$projectUrl.git"
val projectDescription = "Selects compatible AGP, Kotlin, KSP, R8, and Java versions from the detected IDE platform."

group = "io.github.supermonster003"
version = Properties().let { props ->
    rootDir.resolve("version.properties").takeIf { it.isFile }?.inputStream()?.use { props.load(it) }
    props.getProperty("VERSION_NAME") ?: "0.0.0-SNAPSHOT"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withSourcesJar()
    withJavadocJar()
}

gradlePlugin {
    website.set("$projectUrl/blob/master/.readme/README-en.md")
    vcsUrl.set(projectVcsUrl)

    plugins {
        create("platformVersions") {
            id = "io.github.supermonster003.autojs6-platform-versions"
            implementationClass = "org.autojs.build.platform.PlatformVersionsSettingsPlugin"
            displayName = "AutoJs6 Platform Versions"
            description = projectDescription
            tags.set(listOf("android", "kotlin", "ksp", "compatibility", "version-management"))

            compatibility {
                features {
                    configurationCache = false
                }
            }
        }
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

val testMavenRepositoryDirectory = layout.buildDirectory.dir("test-maven-repository")
val centralStagingRepositoryDirectory = layout.buildDirectory.dir("central-staging-repository")

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("AutoJs6 Gradle Platform Versions")
            description.set(projectDescription)
            url.set(projectUrl)
            inceptionYear.set("2026")

            licenses {
                license {
                    name.set("Mozilla Public License 2.0")
                    url.set("https://www.mozilla.org/en-US/MPL/2.0/")
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    id.set("SuperMonster003")
                    name.set("SuperMonster003")
                    email.set("30370009+SuperMonster003@users.noreply.github.com")
                    url.set("https://github.com/SuperMonster003")
                    organization.set("AutoJs6")
                    organizationUrl.set("https://github.com/SuperMonster003/AutoJs6")
                }
            }

            scm {
                connection.set("scm:git:$projectVcsUrl")
                developerConnection.set("scm:git:$projectVcsUrl")
                url.set(projectUrl)
                tag.set("HEAD")
            }

            issueManagement {
                system.set("GitHub")
                url.set("$projectUrl/issues")
            }
        }
    }

    repositories {
        maven {
            name = "TestMaven"
            url = uri(testMavenRepositoryDirectory)
        }
        maven {
            name = "CentralStaging"
            url = uri(centralStagingRepositoryDirectory)
        }
    }
}

tasks.withType<Jar>().configureEach {
    from(rootProject.layout.projectDirectory.file("LICENSE")) {
        into("META-INF")
    }
}

tasks.named<Jar>("javadocJar") {
    from(rootProject.layout.projectDirectory.file(".readme/README-en.md")) {
        into("META-INF")
        rename { "README.md" }
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

val signingKey = providers.gradleProperty("signingKey")
    .orElse(providers.environmentVariable("SIGNING_KEY"))
val signingPassword = providers.gradleProperty("signingPassword")
    .orElse(providers.environmentVariable("SIGNING_PASSWORD"))
val signingUsesGpgCommand = providers.gradleProperty("signingUseGpgCmd")
    .map { value ->
        value.toBooleanStrictOrNull()
            ?: throw GradleException("signingUseGpgCmd must be either true or false.")
    }
    .orElse(false)
    .get()
val signingHasInMemoryKey = signingKey.map { it.isNotBlank() }.orElse(false).get()
val signingConfigured = signingUsesGpgCommand || signingHasInMemoryKey

check(!(signingUsesGpgCommand && signingHasInMemoryKey)) {
    "Choose either local GPG command signing or an in-memory PGP key, not both."
}

if (signingConfigured) {
    pluginManager.apply("signing")
    extensions.configure<SigningExtension> {
        if (signingUsesGpgCommand) {
            useGpgCmd()
        } else {
            useInMemoryPgpKeys(signingKey.get(), signingPassword.orNull)
        }
        sign(publishing.publications)
    }
}

val cleanTestMavenRepository by tasks.registering(Delete::class) {
    delete(testMavenRepositoryDirectory)
}

val cleanCentralStagingRepository by tasks.registering(Delete::class) {
    delete(centralStagingRepositoryDirectory)
}

val validateCentralPublishing by tasks.registering {
    group = "publishing"
    description = "Checks that a PGP signing method required by Maven Central is configured."

    doLast {
        check(signingConfigured) {
            "Maven Central artifacts must be signed. Set signingKey/signingPassword Gradle properties " +
                    "or SIGNING_KEY/SIGNING_PASSWORD environment variables, or enable local GPG command " +
                    "signing with -PsigningUseGpgCmd=true."
        }
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    when {
        name.endsWith("ToTestMavenRepository") -> dependsOn(cleanTestMavenRepository)
        name.endsWith("ToCentralStagingRepository") ->
            dependsOn(cleanCentralStagingRepository, validateCentralPublishing)
    }
}

val centralBundle by tasks.registering(Zip::class) {
    group = "publishing"
    description = "Builds the signed Maven repository bundle accepted by the Central Publisher Portal."
    dependsOn(
        "publishAllPublicationsToCentralStagingRepository",
        "checkPomFileForPluginMavenPublication",
        "checkPomFileForPlatformVersionsPluginMarkerMavenPublication",
    )
    from(centralStagingRepositoryDirectory)
    exclude("**/maven-metadata.xml*")
    destinationDirectory.set(layout.buildDirectory.dir("central-bundle"))
    archiveFileName.set("${project.name}-${project.version}-central-bundle.zip")

    doFirst {
        check(!project.version.toString().endsWith("-SNAPSHOT")) {
            "Maven Central release versions cannot end with -SNAPSHOT."
        }

        val repositoryRoot = centralStagingRepositoryDirectory.get().asFile
        val primaryArtifacts = repositoryRoot.walkTopDown()
            .filter { it.isFile && it.extension in setOf("jar", "pom", "module") }
            .toList()
        val expectedArtifacts = setOf(
            "${project.name}-${project.version}.jar",
            "${project.name}-${project.version}-sources.jar",
            "${project.name}-${project.version}-javadoc.jar",
            "${project.name}-${project.version}.pom",
            "${project.name}-${project.version}.module",
            "io.github.supermonster003.autojs6-platform-versions.gradle.plugin-${project.version}.pom",
        )
        val publishedNames = primaryArtifacts.mapTo(mutableSetOf()) { it.name }
        check(publishedNames.containsAll(expectedArtifacts)) {
            "Central staging is missing expected artifacts: ${expectedArtifacts - publishedNames}"
        }

        primaryArtifacts.forEach { artifact ->
            listOf(".asc", ".md5", ".sha1").forEach { suffix ->
                val companion = artifact.resolveSibling(artifact.name + suffix)
                check(companion.isFile && companion.length() > 0L) {
                    "Central staging is missing ${companion.name} for ${artifact.name}."
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(
        "checkPomFileForPluginMavenPublication",
        "checkPomFileForPlatformVersionsPluginMarkerMavenPublication",
    )
}
