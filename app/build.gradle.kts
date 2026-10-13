plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Optional CI signing. The keystore and passwords come from environment variables
// (GitHub Actions secrets) and are never stored in this repository.
val ciKeystore: String? = System.getenv("KEYSTORE_FILE")?.takeIf { file(it).exists() }

android {
    namespace = "com.example.sensordash"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.sensordash"
        minSdk = 26
        // 35 on purpose: apps targeting 36 can have the landscape lock ignored on tablets.
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-phase1"
    }

    if (ciKeystore != null) {
        signingConfigs {
            create("ci") {
                storeFile = file(ciKeystore)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            if (ciKeystore != null) signingConfig = signingConfigs.getByName("ci")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
