pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://www.jitpack.io")
        //noinspection JcenterRepositoryObsolete
        jcenter()
        maven("https://jitpack.io")

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //noinspection JcenterRepositoryObsolete
        jcenter()
        maven("https://jitpack.io")
        maven("https://www.jitpack.io")
    }
}

rootProject.name = "Gallery"
include(":app")
 