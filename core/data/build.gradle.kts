plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "app.tiebalite.core.data"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
