import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.compose)
}

val keystorePropertiesFile =
    file(
        "${rootProject.projectDir}/keystore.properties",
    )
val keystoreProperties =
    Properties().apply {
        if (keystorePropertiesFile.exists()) {
            keystorePropertiesFile.inputStream().use { load(it) }
        }
    }

android {
    namespace = "app.tiebalite"
    ndkVersion = "29.0.14206865"

    defaultConfig {
        applicationId = "app.tiebalite"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }

    signingConfigs {
        create("release") {
            val storePath = keystoreProperties.getProperty("keystore.file")
            if (!storePath.isNullOrBlank()) {
                storeFile = file(File(rootDir, storePath))
            }
            storePassword = keystoreProperties.getProperty("keystore.password")
                ?: System.getenv("TIEBALITE_KEYSTORE_PASSWORD")
            keyAlias = keystoreProperties.getProperty("keystore.key.alias")
                ?: System.getenv("TIEBALITE_KEY_ALIAS")
                ?: "tiebalite"
            keyPassword = keystoreProperties.getProperty("keystore.key.password")
                ?: System.getenv("TIEBALITE_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.animation.graphics)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":feature:myforums"))
    implementation(project(":feature:explore"))
    implementation(project(":feature:messages"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:history"))
    implementation(project(":feature:thread"))
    implementation(project(":feature:imageviewer"))
    implementation(project(":feature:forum"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
