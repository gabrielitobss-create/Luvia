plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val backendBaseUrl = providers.gradleProperty("LUVIA_BACKEND_URL")
    .orElse("http://10.0.2.2:8080/")
    .get()

android {
    namespace = "com.luvia.ai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.luvia.ai"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "BACKEND_BASE_URL", "\"$backendBaseUrl\"")
    }

    buildFeatures { compose = true; buildConfig = true }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
