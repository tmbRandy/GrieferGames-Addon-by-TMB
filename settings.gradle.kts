pluginManagement {
    repositories {
        maven("https://maven.laby.net/api/v1/maven/release/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id("net.labymod.labygradle.settings") version "0.8.1"
}

rootProject.name = "griefergames-addon-by-tmb"

include(":api")
include(":core")
include(":game-runner")