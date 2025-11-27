plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")

    id("com.android.application")

}

android {
    namespace = "com.example.primerprototipo"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.primerprototipo"
        minSdk = 24
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)



    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
// Activity KTX (para usar 'by viewModels()' fácilmente)
    implementation("androidx.activity:activity-ktx:1.9.2")

    // Import the Firebase BoM - this will manage the versions for other Firebase libraries
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    implementation("com.google.firebase:firebase-analytics")

    // Add the dependency for the Firebase Realtime Database KTX library
    implementation("com.google.firebase:firebase-database-ktx")
    
    // Add the dependency for the Firebase Firestore KTX library (as you had it before)
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Add the dependency for Location Services
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.3")
}