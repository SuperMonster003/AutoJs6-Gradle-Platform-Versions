# SDK XML and test assembly compatibility

IntelliJ IDEA 2026.2 supports the AGP 9.1 line. AGP 9.1.1 brings `sdklib:32.1.1`,
whose repository schema stops at v3, while recent SDK installations contain v4
package metadata. This mismatch emits the SDK processing warning when a build
first resolves its Android SDK. Updating command-line tools alone does not update
the parser inside Gradle.

The settings plugin supplies `sdklib:32.2.1` specifically for AGP 9.1. It retains
`com.android.tools:common` at the version corresponding to the selected AGP patch.
That constraint is necessary because `common` contains `com.android.Version`,
which AGP uses to form AAPT2 coordinates. Updating it independently would combine
the newer AGP version prefix with the older AAPT2 build ID and request an artifact
that does not exist. Other AGP lines retain their existing SDK dependencies.

The native alignment plugin also excludes `UnitTest` and `TestFixtures`
assemblies, as it already excludes `AndroidTest`. Application variant and aggregate
assemblies still run the verifier, and missing APKs remain errors for those tasks.

Run the regression fixture with an installed API 37 SDK:

```shell
./gradlew check
./gradlew -p sample/sdk-xml verifyToolingCompatibility
```

The fixture pins AGP 9.1.1 to reproduce the IDE environment, compiles Android
resources, packages a real APK, runs its native alignment verifier, and assembles
JVM unit tests. It checks the resolved SDK/common versions, AGP identity, and the
absence of an APK verifier on the unit-test assembly. The existing negative native
fixture continues to check that a 4 KB ELF fails verification.

The override is deliberately limited to AGP 9.1. AGP 9.0 (the line IntelliJ IDEA
2026.1 supports) ships `sdklib:32.0.1`, which also stops at repository schema v3,
so that line still prints the warning; its wider `common` gap has not been
verified against `sdklib:32.2.1` and is left untouched.
