plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.meirini.tbs_prediction"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.meirini.tbs_prediction"
        minSdk = 26
        targetSdk = 36
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigasi antar halaman
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Retrofit untuk koneksi ke API Machine Learning
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Lottie untuk animasi di Splash Screen
    implementation("com.airbnb.android:lottie:6.4.0")

    // Firebase Authentication (untuk login/register)
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")

// Firebase Firestore (untuk simpan data user/petani)
    implementation("com.google.firebase:firebase-firestore-ktx:25.0.0")

// Import the BoM (agar versi library sinkron)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    // Library untuk Grafik Line Chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}