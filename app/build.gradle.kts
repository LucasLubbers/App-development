plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.workoutbuddyapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.workoutbuddyapplication"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true // Enabling Compose
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15" // You can also update this to the latest version
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
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.runtime.android)
    implementation(libs.transport.runtime)
    implementation(libs.javax.inject)
    implementation(libs.dagger)
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.play.services.location)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.play.services.mlkit.barcode.scanning)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.hilt.android.v2562)
    implementation(libs.androidx.hilt.navigation.compose.v110)
    implementation(libs.kotlin.stdlib)
    implementation(libs.hilt.android.v2562)
    implementation(libs.androidx.datastore.preferences)

    // Adding the necessary Compose dependencies
    implementation("androidx.compose.ui:ui:1.8.0") // Compose UI
    implementation("androidx.compose.compiler:compiler:1.5.15") // Compose Compiler for Kotlin 2.0
    //noinspection UseTomlInstead
    implementation("com.google.dagger:hilt-android:2.56.2")
}
