pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://artifactory.mercadolibre.com/artifactory/android") }
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://artifacts.mercadopago.com") // ✅ também aqui, para garantir
    }

    dependencies {
        classpath("com.google.gms:google-services:4.3.15")
    }
}

rootProject.name = "APK STELLA D 'ITALIA2.0"
include(":app")
