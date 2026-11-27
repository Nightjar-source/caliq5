import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}


android {
    namespace = "com.Nightjar.caliq5"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.Nightjar.caliq5"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 51490
        versionName = "5.14.90"
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = false
        }
        dex {
            useLegacyPackaging = false
        }
    }

    val keystoreProperties = Properties()
    val keystorePropertiesFile = rootProject.file("local.properties")
    if (keystorePropertiesFile.exists()) {
        FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
    }

    // AdMob IDs: GitHub Secrets (env) -> local.properties -> Google Official Test IDs
    val admobAppId = System.getenv("ADMOB_APP_ID")?.takeIf { it.isNotBlank() }
        ?: keystoreProperties.getProperty("ADMOB_APP_ID")?.takeIf { it.isNotBlank() }
        ?: "ca-app-pub-3940256099942544~3347511713"
    val admobBannerId = System.getenv("ADMOB_BANNER_ID")?.takeIf { it.isNotBlank() }
        ?: keystoreProperties.getProperty("ADMOB_BANNER_ID")?.takeIf { it.isNotBlank() }
        ?: "ca-app-pub-3940256099942544/9214589741"
    val admobInterstitialId = System.getenv("ADMOB_INTERSTITIAL_ID")?.takeIf { it.isNotBlank() }
        ?: keystoreProperties.getProperty("ADMOB_INTERSTITIAL_ID")?.takeIf { it.isNotBlank() }
        ?: "ca-app-pub-3940256099942544/1033173712"

    defaultConfig {
        manifestPlaceholders["ADMOB_APP_ID"] = admobAppId
        buildConfigField("String", "ADMOB_BANNER_ID", "\"$admobBannerId\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$admobInterstitialId\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("Nightjar.keystore")
            storePassword = keystoreProperties.getProperty("KEYSTORE_PASSWORD")
            keyAlias = "Nightjar"
            keyPassword = keystoreProperties.getProperty("KEY_PASSWORD")
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            optIn.addAll(
                "androidx.compose.foundation.layout.ExperimentalLayoutApi",
                "androidx.compose.material3.ExperimentalMaterial3Api"
            )
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.profileinstaller) // Baseline Profile installer for Android 7-9
    implementation(libs.play.services.ads)
}

