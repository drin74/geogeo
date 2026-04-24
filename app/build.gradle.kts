plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")  // ← Без версии!
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.geogeo"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.geogeo"
        minSdk = 26
        targetSdk = 33
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
    dependencies {
        // Firebase BOM
        implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

        // ТОЛЬКО Firestore (Analytics НЕ добавляйте!)
        implementation("com.google.firebase:firebase-firestore-ktx")

        // Glide
        implementation("com.github.bumptech.glide:glide:4.16.0")

        // CardView
        implementation("androidx.cardview:cardview:1.0.0")

        // Стандартные зависимости
        implementation("androidx.core:core-ktx:1.12.0")
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.11.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    }
}