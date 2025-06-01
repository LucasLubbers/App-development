import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("gradle.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
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

        buildConfigField("String", "SUPABASE_URL", "\"https://attsgwsxdlblbqxnboqx.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("supabase.anon.key", "YOUR_ANON_KEY")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
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
}

dependencies {
    // Core
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Jetpack Compose
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui.v180)
    implementation(libs.androidx.compiler)

    // Coil for images
    implementation(libs.coil.compose)

    // Hilt for DI
    implementation(libs.hilt.android)
    implementation(libs.places)
    annotationProcessor(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose.v110)

    // DataStore
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.datastore.preferences)

    // Location & Camera APIs
    implementation(libs.play.services.location)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.play.services.mlkit.barcode.scanning)

    // Room
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.runtime.android)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Ktor (for custom networking if needed)
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // Supabase
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:compose-auth:2.0.0") {
        exclude(group = "androidx.compose.material3", module = "material3")
    }
    implementation("io.github.jan-tennert.supabase:compose-auth-ui:2.0.0") {
        exclude(group = "androidx.compose.material3", module = "material3")
    }

    // Other
    implementation(libs.transport.runtime)
    implementation(libs.javax.inject)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
