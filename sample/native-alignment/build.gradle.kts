plugins {
    id("com.android.application") version "9.0.1"
    id("io.github.supermonster003.autojs6-native-alignment")
}
android {
    namespace = "org.autojs.test.nativealignment"
    compileSdk = 36
    defaultConfig { minSdk = 24; targetSdk = 36; applicationId = namespace }
    packaging.jniLibs.useLegacyPackaging = true
    packaging.jniLibs.keepDebugSymbols += "**/*.so"
}
