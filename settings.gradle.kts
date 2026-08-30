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
        // Use JitPack with www to ensure resolution in all runners
        maven { url = uri("https://www.jitpack.io") }
    }
}

rootProject.name = "Security-"
include(":app")
