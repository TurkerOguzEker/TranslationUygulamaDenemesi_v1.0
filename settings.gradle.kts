pluginManagement {
    repositories {
        google() // Kısıtlamaları kaldırdık, artık eklentiyi bulabilir
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TranslationUygulamaDenemesi_v1.0"
include(":app")