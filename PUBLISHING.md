# Publishing AutoJs6 Gradle Platform Versions

This document describes the maintainer workflow. It deliberately contains no credentials or private key material.

## Permanent identifiers

- Maven group: `io.github.supermonster003`
- Maven artifact: `autojs6-gradle-platform-versions`
- Gradle plugin ID: `io.github.supermonster003.autojs6-platform-versions`
- Source repository: `https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions`

The Java/Kotlin package remains `org.autojs.build.platform`; package names do not have to match Maven coordinates or
the Gradle plugin ID.

## Local verification

Run the complete build, plugin validation, unit tests, and Maven Central POM checks:

```powershell
.\gradlew.bat clean check
```

Publish both the implementation component and plugin marker to an isolated repository under `build/`:

```powershell
.\gradlew.bat publishAllPublicationsToTestMavenRepository
```

The repository is written to `build/test-maven-repository`. It is separate from `mavenLocal()`, so consumer tests do
not accidentally resolve a stale artifact from the developer's home directory.

## Signing credentials

Maven Central requires PGP signatures. The release key has the following public identity:

```text
UID:         SuperMonster003 <30370009+SuperMonster003@users.noreply.github.com>
Fingerprint: 6533 227D 9B22 7132 07B4  4CA5 3278 716E 2E61 74D7
Long key ID: 3278716E2E6174D7
Key server:  keyserver.ubuntu.com
```

For CI, the build accepts an ASCII-armored private key and its passphrase from either Gradle properties or environment
variables:

| Gradle property | Environment variable |
|---|---|
| `signingKey` | `SIGNING_KEY` |
| `signingPassword` | `SIGNING_PASSWORD` |

For local release work, Gradle can instead use the GPG keyring and `gpg-agent`. Put Git's GPG directory on `PATH`,
select the release key by its public long ID, and enable command-based signing:

```powershell
$env:Path = 'C:\Program Files\Git\usr\bin;' + $env:Path
.\gradlew.bat centralBundle `
  -PsigningUseGpgCmd=true `
  '-Psigning.gnupg.keyName=3278716E2E6174D7'
```

GPG obtains the passphrase from its agent or secure Pinentry window; it is not passed to Gradle. Signing remains
disabled for ordinary credential-free builds. The `centralBundle` task fails explicitly when neither local GPG nor an
in-memory signing key is configured.

Never put a private key, passphrase, Central token, or Plugin Portal key in this repository, `local.properties`, a
command-line argument, build output intended for upload, or a commit. Store local secrets outside the repository and
use protected CI secrets for automation.

## Maven Central bundle

With in-memory signing variables present, build the upload archive:

```powershell
.\gradlew.bat centralBundle
```

The archive is written to:

```text
build/central-bundle/autojs6-gradle-platform-versions-<version>-central-bundle.zip
```

The task publishes both Maven components into a clean staging directory, checks both POM files, verifies the expected
JAR/POM/module files and their `.asc`, `.md5`, and `.sha1` companions, excludes repository-level `maven-metadata.xml`,
and then creates a reproducible ZIP in Maven repository layout.

The protected `Publish release` GitHub Actions workflow is the canonical remote publisher. It always uploads this
bundle as `USER_MANAGED`, waits for `VALIDATED`, verifies the returned deployment UUID, name, and every PURL, then
calls the official Central Publisher API to finalize it and waits for both `PUBLISHED` and public Maven resolution.
This preserves a distinct validation checkpoint without depending on Central Portal login. A published Maven Central
version is immutable and cannot be replaced.

If an earlier run stopped after upload, use the protected `Finalize Central deployment` workflow with the existing
UUID and matching release tag. Its `inspect` operation is read-only; `publish` performs the same identity checks and
API finalization, waits for public artifacts, and can complete a missing GitHub Release after both registries resolve.
Never upload the same immutable version again merely because the Portal UI or OAuth login is unavailable.

## Gradle Plugin Portal

The Plugin Publish plugin reads its credentials from the standard CI-friendly variables:

| Gradle property | Environment variable |
|---|---|
| `gradle.publish.key` | `GRADLE_PUBLISH_KEY` |
| `gradle.publish.secret` | `GRADLE_PUBLISH_SECRET` |

Validate without uploading a plugin version:

```powershell
.\gradlew.bat publishPlugins --validate-only
```

Publish after Maven Central validation and the release checklist are complete:

```powershell
.\gradlew.bat publishPlugins
```

The first Plugin Portal publication undergoes manual review. The configured website points to the English README and
the VCS URL points to the public GitHub repository.

## Release checklist

1. Choose a version that has never been used by a Git tag, GitHub Release, Maven Central, or the Plugin Portal.
2. Update `VERSION_NAME`, `VERSION_BUILD`, `.readme/common.json`, and every localized changelog, then regenerate all
   Markdown files. Pure scheduled data releases perform this step automatically and increment the patch component.
3. Run translation checks, scraper tests, `clean check`, the headless sample, and the isolated Maven publication test.
4. Commit the exact release source and create an annotated `v<version>` Git tag. For scheduled data releases, the
   workflow verifies that `master` has not advanced and pushes the commit and tag atomically.
5. Run `Publish release` from that tag with `targets=both` and approve its protected `release` Environment deployment.
6. Let the workflow build and sign the Central bundle, upload it as `USER_MANAGED`, verify `VALIDATED`, finalize it
   through the official API, and wait for public Maven Central resolution.
7. Let the same protected job publish the Gradle plugin and wait for public Plugin Portal resolution.
8. Confirm that the workflow creates the GitHub Release only after both public registries expose the marker and
   implementation artifacts.
9. If a target fails after the other is immutable, rerun only that target. If Central already has a validated upload,
   finalize its UUID instead of creating a duplicate deployment.
10. Verify a fresh consumer using only public repositories before updating downstream projects.

Official references:

- Maven Central requirements: <https://central.sonatype.org/publish/requirements/>
- Central bundle upload: <https://central.sonatype.org/publish/publish-portal-upload/>
- Central Publisher API: <https://central.sonatype.org/publish/publish-portal-api/>
- Gradle Plugin Portal publication: <https://plugins.gradle.org/docs/publish-plugin>
