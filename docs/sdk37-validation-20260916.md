# SDK 37 validation

Date: 2026/09/16. Project version: 1.8.2, build 77.

The Gradle plugin itself is a JVM project. Android SDK levels are declared by its
two consumer samples, which now both use compile SDK 37 and target SDK 37:

| Consumer | Compile SDK | Target SDK | AGP |
| --- | --- | --- | --- |
| `sample` | 37 | 37 | Automatically selected, 9.1.1 with the repository wrapper |
| `sample/native-alignment` | 37 | 37 | 9.1.1 |

The repository wrapper uses Gradle 9.3.1. The negative fixture explicitly uses
Build Tools 37.0.0, and CI installs `platforms;android-37.0` and
`build-tools;37.0.0`. These versions meet the
[official AGP 9.1.1 compatibility requirements](https://developer.android.com/build/releases/agp-9-1-0-release-notes?hl=en).
The bundled Android API compatibility data already maps API 37.0 to minimum AGP
9.1.1. The existing headless facade test now exercises integer API 37 with Gradle
9.5.0 and verifies that AGP 9.3.2 may be selected above that lower boundary.

Validation used Windows 11, Oracle JDK 17.0.12, Gradle 9.3.1, and the installed
Android SDK 37.0 platform. The following commands ran from the repository root:

```powershell
.\gradlew.bat build --no-daemon --max-workers=2 --console=plain
.\gradlew.bat -p sample printPlatformVersions --offline --no-daemon --max-workers=2 --console=plain
.\gradlew.bat -p sample/native-alignment assembleDebug --offline --no-daemon --max-workers=2 --console=plain
npm --prefix .utils test
py -X utf8 .python/check_translations.py
py -X utf8 .python/generate_markdown.py
```

- Full Gradle build: successful in 1m 9s. All 90 JVM tests passed, with zero
  failures, errors, or skipped tests. Plugin validation and all three publication
  POM checks passed.
- Headless consumer: successful in 33s. Selected AGP 9.1.1 with minimum AGP 9.1.1,
  Kotlin 2.2.21, and KSP 2.2.21-2.0.5. The actual root buildscript KGP was also
  2.2.21, satisfying the existing classpath assertion.
- Negative fixture: APK assembly completed, then
  `verifyDebugNativePageAlignment` rejected the intended library in 35s. The fresh
  receipt had `summary.ok=false`, `summary.skipped=false`, and exactly one error:
  `libunaligned.so [arm64-v8a]: PT_LOAD alignment 4096 < 16384`. This expected
  failure is the fixture's acceptance condition.
- Build Tools 37.0.0 `aapt2 dump badging` confirmed the generated APK contains
  `compileSdkVersion='37'` and `targetSdkVersion:'37'`.
- All 9 Node tests passed. All 10 language sources passed translation checks.
  A second Markdown generation left the SHA-256 hashes of all 21 generated files
  unchanged.
- The wrapper JAR is byte-identical to the wrapper embedded in the Gradle 9.3.1
  distribution. Its SHA-256 is
  `b3a875ddc1f044746e1b1a55f645584505f4a10438c1afea9f15e92a7c42ec13`.

The negative APK is
`sample/native-alignment/build/outputs/apk/debug/native-alignment-negative-sample-debug.apk`.
Its SHA-256 is
`0373898e35aa7a65ff1f0f77c20c5f11fdb7246aded453b4c7dbbd7ed0f66869`.
It contains a synthetic, intentionally non-executable ELF and is used only for
build-time verification.

Local logs and APK badging are under `build/verification/sdk37-20260916/`.
The alignment receipt is
`sample/native-alignment/build/reports/native-alignment/Debug.json`.
JUnit XML results are under `build/test-results/test/`.
