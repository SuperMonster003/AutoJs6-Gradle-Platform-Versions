plugins {
    id("com.android.application") version "9.1.1"
    id("io.github.supermonster003.autojs6-native-alignment")
}
android {
    namespace = "org.autojs.test.nativealignment"
    compileSdk = 37
    buildToolsVersion = "37.0.0"
    defaultConfig { minSdk = 24; targetSdk = 37; applicationId = namespace }
    packaging.jniLibs.useLegacyPackaging = true
    packaging.jniLibs.keepDebugSymbols += "**/*.so"
}
