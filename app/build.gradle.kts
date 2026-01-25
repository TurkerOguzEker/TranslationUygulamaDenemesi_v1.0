plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services")
}

android {
    namespace = "turkeroguz.eker.translationuygulamadenemesi_v10"
    // Stabil bir deneyim için 35 kullanmanızı öneririm (36 çok yeni)
    compileSdk = 35

    defaultConfig {
        applicationId = "turkeroguz.eker.translationuygulamadenemesi_v10"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        viewBinding = true // XML sayfalarınızın (item_book vb.) çalışması için şart
        buildConfig = true
    }

    // Kotlin 2.0+ kullandığınız için Compose Compiler artık Kotlin içine gömülüdür.
    // composeOptions ve kotlinCompilerExtensionVersion satırlarını silebilirsiniz.
}

dependencies {
    // dependencies bloğuna eklenecekler
    implementation("com.google.firebase:firebase-auth-ktx:23.1.0") // Giriş/Kayıt için
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.1") // Hikayeler ve Kullanıcılar için
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1") // Resimler için
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // XML ve ViewPager2 bileşenleri
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}