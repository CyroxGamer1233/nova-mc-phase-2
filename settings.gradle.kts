rootProject.name = "NovaMC"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include("common", "launcher", "updater", "client")
