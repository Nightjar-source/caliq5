// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Kotlin Multiplatform Plugin 2.1.21
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    // Compose Multiplatform Plugin 1.8.0
    alias(libs.plugins.composeMultiplatform) apply false
    // Compose Compiler Plugin
    alias(libs.plugins.composeCompiler) apply false
    // Android Gradle Plugin 8.9.0 (last compatible with KMP 2.1.x)
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
}
