plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.ktlint)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}

android {
    namespace = "app.tiebalite.core.ui"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.core.ktx)
    api(libs.material.color.utilities)
    api(project(":core:data"))
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
}
