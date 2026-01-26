pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // MPAndroidChart ve diğer GitHub kaynaklı kütüphaneler için bu satır ŞARTTIR:
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "TranslationUygulamaDenemesi_v1.0"
include(":app")