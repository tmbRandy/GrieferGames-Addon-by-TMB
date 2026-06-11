package tmb.randy.tmbgriefergames.core;

public final class Const {

    private Const() {}

    public static final class Menu {
        private Menu() {}

        // Crafting & Recipe Menus
        public static final String CUSTOM_KATEGORIEN                = "§6Custom-Kategorien";
        public static final String MINECRAFT_REZEPTE                = "§6Minecraft-Rezepte";
        public static final String VANILLA_BAUANLEITUNG             = "§6Vanilla Bauanleitung";

        // Compression
        public static final String ITEM_KOMPRIMIERUNG               = "§6Item-Komprimierung";
        public static final String ITEM_KOMPRIMIERUNG_BAUANLEITUNG  = "§6Item-Komprimierung-Bauanleitung";
        public static final String VARIANT_PREFIX                   = "§6Variante ";

        // Hopper
        public static final String TRICHTER_EINSTELLUNGEN          = "§6Trichter-Einstellungen";
        public static final String TRICHTER_MEHRFACH_VERBINDUNGEN  = "§6Trichter-Mehrfach-Verbindungen";

        // Item Shifter
        public static final String WAEHLE_KOMPRIMIERUNG            = "§6Wähle deine Komprimierung";
        public static final String SPEZIELLE_ITEMS                 = "§6spezielle Items";
        public static final String SPAWNER_LAGER                   = "§6Spawner - Lager";

        // Potion Timer
        public static final String TRANK_BENUTZEN                  = "§6Möchtest du den Trank benutzen?";

        // Eject / Storage
        public static final String LAGER_PREFIX                    = "§0Lager: ";
    }

    public static final class Chat {
        private Chat() {}

        // Hopper
        public static final String TRICHTER_CONNECT_START          = "[Trichter] Das Verbinden wurde aktiviert. Klicke auf den gewünschten Endpunkt.";
        public static final String TRICHTER_MULTI_CONNECT_START    = "[Trichter] Das Multi-Verbinden wurde aktiviert. Klicke mit dem gewünschten Item auf den gewünschten Endpunkt.";
        public static final String TRICHTER_CONNECTED              = "[Trichter] Der Trichter wurde erfolgreich verbunden.";
        public static final String TRICHTER_CONNECT_ENDED          = "[Trichter] Der Verbindungsmodus wurde beendet.";
        public static final String TRICHTER_START_TOO_FAR          = "[Trichter] Der Startpunkt ist zu weit entfernt. Bitte starte erneut.";
        public static final String TRICHTER_MULTI_ADDED            = "[Trichter] Die Multi-Verbindung wurde hinzugefügt.";
        public static final String TRICHTER_MULTI_REMOVED          = "[Trichter] Die Mehrfach-Verbindungen wurden aufgehoben.";
        public static final String TRICHTER_REMOVED                = "[Trichter] Die Verbindung wurde aufgehoben.";

        // AutoCrafter
        public static final String CHEST_IN_USE                    = "Du kannst diese Kiste nicht öffnen, solange sie von einem anderen Spieler benutzt wird.";
        public static final String NO_VANILLA_RECIPE               = "[Rezepte] Es konnte kein Vanilla Rezept für dieses Item gefunden werden.";

        // AutoHopper
        public static final String PLOT_BORDER_REACHED             = "Das Ende vom Grundstück wurde erreicht.";

        // AutoLoot
        public static final String SWITCHER_DATA_DOWNLOADED        = "[Switcher] Daten heruntergeladen!";
        public static final String CASE_OPENING_TIMER_START        = "[CaseOpening] Du kannst erst am ";
        public static final String CASE_OPENING_TIMER_END          = " wieder Free-Kisten abholen.";
        public static final String CASE_OPENING_RECEIVED           = "[CaseOpening] Du hast 2 Kisten erhalten.";
        public static final String FREE_BOOSTER_TIMER_START        = "Du kannst erst am ";
        public static final String FREE_BOOSTER_TIMER_END          = " wieder einen Free-Booster abholen.";
        public static final String FREE_BOOSTER_RECEIVED_START     = "Du hast 1 ";
        public static final String FREE_BOOSTER_RECEIVED_END       = "-Booster erhalten. Danke für deine Unterstützung von GrieferGames!";
        public static final String KOPF_START                      = "[Kopf] Du hast einen ";
        public static final String KOPF_END                        = "-Kopf erhalten!";

        // GrieferGames Server
        public static final String TELEPORTED_TO_PLOT              = "[GrieferGames] Du wurdest zum Grundstück teleportiert.";
        public static final String AUTOSWITCH_START                = "[GrieferGames] Du wurdest automatisch auf ";
        public static final String AUTOSWITCH_END                  = " verbunden.";
        public static final String SERVERSWITCH_START              = "[GrieferGames] Serverwechsel auf ";
        public static final String SERVERSWITCH_END                = " wurde gestartet..";
        public static final String GG_DOWNLOAD                     = "[GrieferGames] Download: https://mysterymod.net/download/";
        public static final String GG_MYSTERYMOD                   = "[GrieferGames] Wir sind optimiert für MysteryMod. Lade Dir gerne die Mod runter!";
        public static final String GG_PORTALRAUM                   = "[GrieferGames] Du bist im Portalraum. Wähle deinen Citybuild aus.";
        public static final String GG_DATA_DOWNLOADED              = "[GrieferGames] Deine Daten wurden vollständig heruntergeladen.";
        public static final String GG_TELEPORT_WAIT                = "[GrieferGames] Bitte warte 12 Sekunden zwischen jedem Teleport.";
        public static final String GG_JOIN_WAIT                    = "[GrieferGames] Bitte warte 15 Sekunden zwischen jedem Join-Versuch.";
        public static final String GG_DAYTIME_UPDATED              = "[GrieferGames] Deine Tageszeit wurde vom Grundstück aktualisiert.";
        public static final String GG_DAYTIME_RESTORED             = "[GrieferGames] Deine Tageszeit wurde wiederhergestellt.";
        public static final String GGAUTH_VERIFIED                 = "[GGAuth] Du wurdest erfolgreich verifiziert.";
        public static final String ALREADY_CONNECTING              = "Already connecting to this server!";
        public static final String SERVER_STATUS                   = "------------ [ Server-Status ] ------------";
        public static final String ERGRIFFENE_MASSNAHMEN           = "Ergriffene Maßnahmen:";
        public static final String PORTALRAUM_CONNECTING           = "Versuche in den Portalraum zu verbinden.";
        public static final String NEWS_DELIMITER                  = "------------ [ News ] ------------";

        // Switcher / StartKick / StartJail
        public static final String SWITCHER_LOADING                = "[Switcher] Lade Daten herunter!";
        public static final String STARTKICK_PREFIX                = "[StartKick] Ersteller: ";
        public static final String STARTJAIL_PREFIX                = "[StartJail] Ersteller: ";

        // Chat Filter
        public static final String FREUNDE_KEINE_ANFRAGEN          = "[Freunde] Du hast aktuell keine Freundschaftsanfragen.";
        public static final String REZEPTE_NOT_ENOUGH_MATERIAL     = "[Rezepte] Du hast nicht genug Material, um dieses Rezept herzustellen.";
        public static final String CASEOPENING_PRIZE_DRAWN         = "[CaseOpening] Folgender Preis wurde gezogen: ";
        public static final String CASEOPENING_PLAYER_WON_START    = "[CaseOpening] Der Spieler ";
        public static final String CASEOPENING_PLAYER_WON_END      = " hat einen Hauptpreis gewonnen!";
        public static final String CASEOPENING_MAIN_PRIZE          = "[CaseOpening] Ein Spieler hat einen Hauptpreis gewonnen!";
        public static final String CASEOPENING_KRISTALLE           = "[CaseOpening] Du hast 72 Kristalle auf deinem Konto.";
        public static final String LUCKYBLOCK_PREFIX               = "[LuckyBlock] ";
        public static final String TEAM_ADMIN_PREFIX               = "[TEAM] Admin ┃ ";
        public static final String TEAM_ADMIN_END                  = " » Hey Leute, was geht?";
        public static final String STREAMER_TAG                    = "§8[§6Streamer§8]";

        // AutoFisher
        public static final String AUTOFISHER_ITEMS_WARNING        = "Warnung! Die auf dem Boden liegenden Items werden in 20 Sekunden entfernt!";
        public static final String AUTOFISHER_ITEMS_REMOVED        = "auf dem Boden liegende Items entfernt!";
    }

    public static final class Cmd {
        private Cmd() {}

        public static final String REZEPTE                         = "/rezepte";
        public static final String SWITCH                          = "/switch ";
        public static final String PLOT_HOME                       = "/p h ";
        public static final String HOME                            = "/home ";
        public static final String WARP                            = "/warp ";
        public static final String PORTAL                          = "/portal";
        public static final String TPACCEPT                        = "/tpaccept";
        public static final String JA                              = "/ja";
        public static final String GRIEFERBOOST                    = "/grieferboost";
        public static final String FREEKISTE                       = "/freekiste";
    }

    public static final class Comp {
        private Comp() {}

        public static final String LEVEL_PREFIX                    = "§6Komprimierungsstufe ";
        public static final String LEVEL_1                         = LEVEL_PREFIX + "1";
        public static final String LEVEL_2                         = LEVEL_PREFIX + "2";
        public static final String LEVEL_3                         = LEVEL_PREFIX + "3";
        public static final String LEVEL_4                         = LEVEL_PREFIX + "4";
        public static final String LEVEL_5                         = LEVEL_PREFIX + "5";
        public static final String LEVEL_6                         = LEVEL_PREFIX + "6";
        public static final String LEVEL_7                         = LEVEL_PREFIX + "7";
    }

    public static final class Lore {
        private Lore() {}

        public static final String VERBUNDEN_MIT_LABEL             = "Verbunden mit:";
        public static final String VERBUNDEN_MIT_PREFIX            = "§7Verbunden mit: §e";
        public static final String WEITERLEITEN_AN                 = "Weiterleiten an ";
        public static final String VERFUEGBAR_COLOR                = "§e";
        public static final String VERFUEGBAR_SUFFIX               = " Verfügbar";
    }

    public static final class Item {
        private Item() {}

        public static final String FLUGTRANK                       = "§6Flugtrank";
        public static final String ABBAUTRANK                      = "§6Abbautrank";
        public static final String ENDERTRUHE                      = "Endertruhe";
    }
}
