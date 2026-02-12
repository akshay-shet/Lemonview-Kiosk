pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    plugins {
        // Android Gradle Plugin
        id("com.android.application") version "8.2.2" apply false

        // Kotlin Android plugin
        id("org.jetbrains.kotlin.android") version "1.9.22" apply false
        id("com.google.gms.google-services") version "4.4.2" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Lemonview"
include(":app")