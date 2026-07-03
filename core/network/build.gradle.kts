plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "app.tiebalite.core.network"

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.okhttp.logging)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":core:model"))
    api(project(":core:proto"))
    testImplementation(libs.junit)
    testImplementation(libs.org.json)
}
