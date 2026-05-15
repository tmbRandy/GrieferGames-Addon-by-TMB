rootProject.name = "griefergames-addon-by-tmb"

pluginManagement {
    repositories {
        maven("https://dist.labymod.net/api/v1/maven/release/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "net.labymod.labygradle.settings") {
                useModule("net.labymod.gradle:common:${requested.version}")
            }
        }
    }
}

plugins {
    id("net.labymod.labygradle.settings") version "0.6.2"
}

include(":api")
include(":core")
