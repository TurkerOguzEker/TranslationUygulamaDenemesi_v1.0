// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // HATAYI ÇÖZEN SATIR BU: Sürüm numarası ve 'apply false' eklendi
    id("com.google.gms.google-services") version "4.4.2" apply false

}