plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

val supabaseAnonKey = project.findProperty("supabase.anon.key") as String? ?: ""

android {
    namespace = "com.example.workoutbuddyapplication"
    compileSdk = 35

    buildFeatures {
        compose = true // Enabling Compose
        buildConfig = true // Enable buildConfig generation
    }

    defaultConfig {
        applicationId = "com.example.workoutbuddyapplication"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"https://attsgwsxdlblbqxnboqx.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF0dHNnd3N4ZGxibGJxeG5ib3F4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc3MzU2ODQsImV4cCI6MjA2MzMxMTY4NH0.m5cEVSpbrgbEM6-OPiIn7gNmClncxmwcY_UfW_2uK-s\"")
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
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Adding the necessary Compose dependencies
    implementation(libs.androidx.ui.v180) // Compose UI
    implementation(libs.androidx.compiler) // Compose Compiler for Kotlin 2.0
    //noinspection UseTomlInstead
    implementation("com.google.dagger:hilt-android:2.56.2")

    // Supabase dependencies
    implementation(platform("io.github.jan-tennert.supabase:bom:2.0.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:compose-auth") {
        exclude(group = "androidx.compose.material3", module = "material3")
    }
    implementation("io.github.jan-tennert.supabase:compose-auth-ui") {
        exclude(group = "androidx.compose.material3", module = "material3")
    }
    
    // Ktor dependencies
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    
    // Kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
