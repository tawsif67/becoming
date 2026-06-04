plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // CHANGE 1.9.0 to 2.2.10
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
}
android {
    namespace = "com.example.becoming"

    // CHANGE THIS: Use a stable SDK version
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.becoming"
        minSdk = 26 // Increased to 26 as required by the AI SDK
        targetSdk = 34 // Changed to 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Fixed syntax for Kotlin DSL
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

// Keep your dependencies block exactly as you have it!

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

// Extended Icons (for Swords, Maps, etc.)
    implementation("androidx.compose.material:material-icons-extended")

// Gemini AI SDK
    implementation("com.google.ai.client.generativeai:generativeai:0.6.0")

// Kotlin Serialization (for parsing AI JSON)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

