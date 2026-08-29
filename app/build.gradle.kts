plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.securitynav.security"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.securitynav.security"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    // Android Base
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Red y Serialización (Retrofit y Gson)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Base de datos cifrada (SQLCipher)
    implementation("net.zetetic:android-database-sqlcipher:4.6.1")
    implementation("androidx.sqlite:sqlite:2.4.0")

    // Gráficos en tiempo real (MPAndroidChart vía JitPack)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
