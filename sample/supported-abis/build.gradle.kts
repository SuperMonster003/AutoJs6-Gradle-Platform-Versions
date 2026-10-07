// Positive fixture: three packaged ABIs narrowed by two ABI splits must yield the meta-data
// "arm64-v8a,x86_64" in every output, including the universal APK.
plugins {
    id("com.android.application") version "9.1.1"
    id("io.github.supermonster003.autojs6-native-alignment")
}
android {
    namespace = "org.autojs.test.supportedabis"
    compileSdk = 37
    buildToolsVersion = "37.0.0"
    defaultConfig { minSdk = 24; targetSdk = 37; applicationId = namespace }
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "x86_64")
            isUniversalApk = true
        }
    }
}
