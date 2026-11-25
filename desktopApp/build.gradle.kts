import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "com.Nightjar.caliq5.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Dmg, TargetFormat.Deb)
            packageName = "Caliq 5"
            packageVersion = "5.14.86"
            description = "حاسبة الإعفاء والسعي لطلاب وطالبات العراق - كاليك 5"
            vendor = "Nightjar"

            windows {
                menuGroup = "Caliq 5"
                shortcut = true
                perUserInstall = true
                upgradeUuid = "7b8f9c2a-4d3e-4b1a-9f8e-1234567890ab"
            }
        }
    }
}
