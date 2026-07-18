plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.rokusodo.healthcheckup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rokusoudo.healthcheckup"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // CameraX
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // ML Kit Text Recognition (Japanese)
    implementation(libs.mlkit.text.recognition.japanese)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Navigation Component
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // MPAndroidChart
    implementation(libs.mpandroidchart)

    // Firebase (BOM + Auth)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

    // Google Sign-In
    implementation(libs.play.services.auth)

    // CameraX + Firestore の Guava 依存関係競合を回避
    implementation("com.google.guava:guava:32.1.2-android")

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    // Coroutines support for Google Play Services / ML Kit Tasks
    implementation(libs.kotlinx.coroutines.play.services)

    // Unit tests (JVM / Robolectric)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.test.junit.ktx)
    testImplementation(libs.room.testing)
    testImplementation(libs.navigation.testing)
    testImplementation(libs.kotlinx.coroutines.test)
}
