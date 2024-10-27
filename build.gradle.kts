plugins {
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
}

val versions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

group = "tmb.randy"
version = providers.environmentVariable("VERSION").getOrElse("1.7.0")

labyMod {
    defaultPackageName = "tmb.randy.tmbgriefergames"

    minecraft {
        registerVersion(versions.toTypedArray()) {
            runs {
                getByName("client") {
                    // When the property is set to true, you can log in with a Minecraft account
                    // devLogin = true
                }
            }
        }
    }

    addonInfo {
        namespace = "tmbgriefergames"
        displayName = "GrieferGames Addon by TMB"
        author = "TMB Clan"
        description = "Eine Sammlung nützlicher Erweiterungen für das GrieferGames Netzwerk."
        minecraftVersion = "1.8.9,1.12.2"
        version = rootProject.version.toString()
    }
}

subprojects {
    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")

    group = rootProject.group
    version = rootProject.version
}