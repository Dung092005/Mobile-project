plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.chess.stats"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.chess.stats"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.org.json)
}
