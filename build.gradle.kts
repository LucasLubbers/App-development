plugins {
    alias(libs.plugins.android.application) apply(false)
    alias(libs.plugins.kotlin.android) apply(false)
    alias(libs.plugins.compose.compiler) apply(false)
}

buildscript {
    dependencies {
        // Include the Hilt plugin if needed
        classpath(libs.hilt.android.gradle.plugin) // Check for the latest version
        // Include the Compose compiler plugin for Kotlin 2.0 support
        classpath(libs.hilt.android.gradle.plugin.v248)

        classpath("androidx.compose.compiler:compiler:1.5.15") // or the latest version
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    }
}