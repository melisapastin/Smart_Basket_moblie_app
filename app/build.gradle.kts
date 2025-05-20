plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.smartbasket"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.smartbasket"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
}

dependencies {
    // Core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.engage.core)
    implementation(libs.play.services.wallet)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("androidx.compose.foundation:foundation:1.6.0")
    implementation("androidx.compose.foundation:foundation-layout:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.compose.material3:material3:1.2.0")

    // Core dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.0")

    // Compose dependencies
    implementation("androidx.compose:compose-bom:2023.10.01")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Preview support
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.runtime:runtime:1.5.4")

    implementation("androidx.compose.runtime:runtime:1.6.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.material3:material3:1.1.2")

    implementation("androidx.compose.foundation:foundation:1.4.0")  // Or latest version
    implementation("androidx.compose.material3:material3:1.2.0")   // Or latest version
    implementation("androidx.compose.foundation:foundation:1.3.0")
    implementation("androidx.compose.material:material-icons-extended")
}