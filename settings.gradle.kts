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
        maven { url = uri("https://jitpack.io") } // BU SATIRIN OLDUĞUNDAN EMEİN OL
    }
}

rootProject.name = "TranslationUygulamaDenemesi_v1.0"
include(":app")