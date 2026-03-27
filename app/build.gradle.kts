plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.pixelclassics.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pixelclassics.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 10
        versionName = "4.2"
    }

    signingConfigs {
        create("release") {
            val ksFile = file("pixel-classics-release.jks")
            if (ksFile.exists()) {
                storeFile = ksFile
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "pixelclassics2026"
                keyAlias = System.getenv("KEY_ALIAS") ?: "pixelclassics"
                keyPassword = System.getenv("KEY_PASSWORD") ?: "pixelclassics2026"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            val releaseSigning = signingConfigs.findByName("release")
            if (releaseSigning?.storeFile?.exists() == true) {
                signingConfig = releaseSigning
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.webkit:webkit:1.8.0")
    // Go mesh core (gomobile .aar)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
}
