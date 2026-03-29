plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.compose)
}

android {
    namespace = "app.tiebalite.feature.settings"

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
}
