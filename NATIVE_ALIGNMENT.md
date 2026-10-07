# Native page alignment

Plugin `io.github.supermonster003.autojs6-native-alignment` is a project plugin shipped with
Platform Versions 1.8.0. Apply it to Android application modules:

```kotlin
// settings.gradle.kts, inside pluginManagement.plugins
id("io.github.supermonster003.autojs6-native-alignment") version "1.8.0"

// app/build.gradle.kts
plugins { id("io.github.supermonster003.autojs6-native-alignment") }
nativeAlignment {
    strictAbis.set(setOf("arm64-v8a", "x86_64"))
    pageSize.set(16384L)
    // expectNoNativeLibraries.set(true) // for applications without native code
    // checkRelro.set(true)             // optional PT_GNU_RELRO end check
    // scanEmbeddedPayloads.set(true)   // Chaquopy/assets/nested archives in each selected APK
    // alsoScan(layout.buildDirectory.dir("native-runtime-assets"))
    // apkFiles(layout.buildDirectory.file("custom/path/application.apk"))
}
```

`assemble<Variant>` is finalized by `verify<Variant>NativePageAlignment`. The standalone
`verifyNativePageAlignment` checks all built variants without triggering an assembly. All split
APKs listed in AGP's `output-metadata.json` must exist; renamed output files are resolved from
that metadata, not guessed from filenames. Explicit `apkFiles(...)` replaces automatic selection.
Reports are written to `build/reports/native-alignment/<Variant>.json` (`all.json` for the standalone
task), including failures. A report with `summary.ok=false` is never a successful receipt.

The gate checks ELF32/ELF64 headers, PT_LOAD alignment and offset/address congruence. For strict
ABIs, alignment must be at least `pageSize`. Uncompressed libraries additionally require their
actual local ZIP data offsets to be divisible by `pageSize`; DEFLATED libraries only need ELF
alignment. APK signing blocks, ZIP data descriptors and ZIP extra fields are supported. ZIP64 APKs,
encrypted ZIP entries, duplicate names, corrupt ELF headers and incomplete APK metadata are rejected.
No Android SDK, NDK or Python is needed to run this JVM verifier. Only each ELF's first 64 KiB is read.

The binary APK manifest is inspected as well. Reports include `extractNativeLibs` and the numeric
`org.autojs.plugin.contract.NATIVE_PAGE_ALIGNMENT` declaration. A declaration of zero rejects
native code; a positive declaration cannot exceed the measured minimum for strict ABIs. Values
must be powers of two. `android:pageSizeCompat` is rejected because it suppresses the platform's
compatibility warning. A missing alignment declaration means unknown, not zero.

`scanEmbeddedPayloads.set(true)` also inspects each selected APK's assets and nested archives;
those ELF results participate in its declaration check. Use this for Chaquopy or other runtimes
which extract executable code outside `lib/`. It avoids hardcoding variant or generated asset paths.

`alsoScan(...)` reads ELF files and ZIP-compatible nested archives by magic, including `.imy`
archives and extensionless executables. Extraction payloads need ELF alignment, without APK ZIP
alignment. Pass the APK itself to `alsoScan(...)` to inspect native extensions delivered through
assets. Archive nesting is bounded to eight levels. `expectNoNativeLibraries` rejects native code
for every ABI, including 32-bit code; ordinary alignment enforcement defaults to the two 64-bit ABIs.

For local diagnosis, `-Pautojs.nativeAlignment.skip=true` records a skipped, unsuccessful receipt.
The bypass fails when CI/GITHUB_ACTIONS/TF_BUILD/JENKINS_URL/BUILD_BUILDID indicates CI. Release jobs
must preserve/upload the reports and may use SDK build-tools 35+ `zipalign -c -P 16 -v 4` as a second
opinion. Runtime testing on a device reporting `adb shell getconf PAGE_SIZE` as `16384` remains
required: ELF/ZIP alignment cannot detect runtime code that assumes 4 KiB pages.

`sample/native-alignment` is an Android negative fixture with `compileSdk = 37` and
`targetSdk = 37`. It uses AGP 9.1.1 and the repository's Gradle 9.3.1 wrapper. Install
`platforms;android-37.0` and `build-tools;37.0.0` through the SDK manager, then run
`./gradlew -p sample/native-alignment assembleDebug`; assembly must finish with a failure in
`verifyDebugNativePageAlignment`, naming `libunaligned.so` and its 4096-byte PT_LOAD alignment.
The synthetic ELF fixture is intentionally not executable. Unit tests cover positive/negative
ELF/ZIP fixtures, 32-bit handling, nested payloads, missing splits and the CI bypass restriction.

中文: 共享门禁覆盖全部已装配分包, 仅强制 64 位库的 16 KB 对齐. 压缩库只检查 ELF, 未压缩库同时检查
ZIP 数据偏移. `alsoScan` 用于通过 assets 或运行时归档交付的原生库及可执行文件.
没有 APK, 缺失分包或畸形 ELF 均失败; CI 禁止跳过门禁. 设备验收仍需在真实 16 KB 页环境完成.

## Supported ABIs meta-data

Since 1.9.0 the same plugin writes `org.autojs.plugin.contract.SUPPORTED_ABIS` into the merged
manifest of every application variant. The value is derived from the variant's merged native
libraries (`SingleArtifact.MERGED_NATIVE_LIBS`), narrowed by enabled ABI splits and by
`defaultConfig.ndk.abiFilters`, in the order arm64-v8a, armeabi-v7a, x86_64, x86. A package without
native code receives `universal`, which the AutoJs6 host shows as "all". Every output of the variant,
including split APKs and the universal APK, carries the same project-level value: it describes the
ABIs the plugin is built for, while the host derives the installed ABIs from the installed package
itself. The AutoJs6 host prefers this meta-data over the ABI list returned by the INFO binder, and the
official plugin index cross-checks it against release asset names.

```kotlin
nativeAlignment {
    // supportedAbisMetaData.set(false)            // keep a hand-written <meta-data>
    // supportedAbis.set(setOf("arm64-v8a"))       // natives delivered outside lib/, e.g. from assets
    // supportedAbisMetaDataName.set("...")        // another meta-data name
}
```

The transform task is `write<Variant>SupportedAbisMetaData`, scheduled between
`process<Variant>MainManifest` and `process<Variant>Manifest`; an existing `<meta-data>` of the same
name is replaced in place. `sample/supported-abis` packages three ABIs under two enabled ABI splits
and must produce `arm64-v8a,x86_64` in the arm64-v8a, x86_64 and universal APKs
(`./gradlew -p sample/supported-abis assembleDebug`, then `aapt2 dump xmltree --file AndroidManifest.xml`).

中文: 1.9.0 起插件为每个应用变体的合并清单写入 `org.autojs.plugin.contract.SUPPORTED_ABIS`, 取值来自合并后的
原生库目录, 再按已启用的 ABI 分包与 `ndk.abiFilters` 收窄; 无原生代码写入 `universal`. 全部输出 (含分包与
universal APK) 携带同一项目级取值, "已安装 ABI" 由宿主从已安装包自行得出. 可用 `supportedAbis` 覆盖,
`supportedAbisMetaData` 关闭, `supportedAbisMetaDataName` 改名.
