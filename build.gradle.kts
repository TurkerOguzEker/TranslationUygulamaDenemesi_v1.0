// app/build.gradle.kts (App Level)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    // BURADA SADECE ID YAZILIR (Sürüm veya 'apply false' YOK)
    id("com.google.gms.google-services")
}