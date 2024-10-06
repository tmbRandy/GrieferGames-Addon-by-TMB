package tmb.randy.tmbgriefergames.core.widgets;

import net.labymod.api.Laby;
import net.labymod.api.client.entity.Entity;
import net.labymod.api.client.entity.Mob;
import net.labymod.api.client.entity.player.Player;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.widgets.NearbyWidget.NearbyWidgetConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NearbyWidget extends TextHudWidget<NearbyWidgetConfig> {

    private static final List<String> blacklist = Arrays.asList(
        "Adventurer",
        "Admin-Shop",
        "Statistik",
        "Händler",
        "Lotterie",
        "Vote-System",
        "Rand-Schmied",
        "Bürgermeister",
        "Adventurer",
        "Verkäufer",
        "Citybuild",
        "Skyblock Museum",
        "Impressum",
        "Datenschutz",
        "Jobs",
        "Block des Tages",
        "GS-Bewertungen",
        "Auktionshaus"
    );

    private String name;
    private TextLine line;

    public NearbyWidget(HudWidgetCategory category) {
        super("nearby", NearbyWidgetConfig.class);
        this.name = Laby.labyAPI().getName();
        setIcon(Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/nearby.png")));
        this.bindCategory(category);
    }

    @Override
    public void load(NearbyWidgetConfig config) {
        super.load(config);
        this.line = super.createLine(I18n.getTranslation("tmbgriefergames.nearby.nearby"), name);
    }

    @Override
    public void onTick(boolean isEditorContext) {
        name = getPlayersListAsString();
        this.line.updateAndFlush(name);
    }

    @Override
    public boolean isVisibleInGame() {
        return Addon.isGG();
    }

    private String getPlayersListAsString() {
        StringBuilder list = new StringBuilder();
        for (Player player : getPlayersList()) {
            int dist = (int)Math.sqrt(player.getDistanceSquared(Laby.labyAPI().minecraft().getClientPlayer()));
            list.append("\n").append(player.getName()).append(" (").append(dist).append("m)");
        }

        if(config.getShowMobs().get()) {
            MobCounter counter = new MobCounter();
            for (Entity entity : Laby.labyAPI().minecraft().clientWorld().getEntities()) {
                if(entity instanceof Mob mob) {
                    String mobString = mob.toString();
                    String mobName = extractMobInfo(mobString);
                    counter.addMob(mobName);
                }
            }

            list.append("\n").append(counter.getFormattedMobCounts());
        }

        return list.toString();
    }

    public static List<Player> getPlayersList() {
        List<Player> output = new ArrayList<>();

        outerLoop: for (Entity entity : Laby.labyAPI().minecraft().clientWorld().getEntities()) {

            if(entity instanceof Player player) {
                if(Laby.labyAPI().minecraft().getClientPlayer() != null) {
                    if(player.getName().equals(Laby.labyAPI().minecraft().getClientPlayer().getName())) {
                        continue;
                    }
                }

                if(player.getName().startsWith("§6"))
                    continue;

                for (String string : blacklist) {
                    if(player.getName().contains(string))
                        continue outerLoop;
                }

                int dist = (int)Math.sqrt(player.getDistanceSquared(Laby.labyAPI().minecraft().getClientPlayer()));

                if(dist > 1000)
                    continue;

                output.add(player);
            }
        }

        return output;
    }

    public static String extractMobInfo(String input) {
        String cleanInput = input.replaceAll("§[0-9a-fk-or]", "");
        return cleanInput.replaceAll(".*\\['(.*?)'/.*", "$1");
    }


    public static class MobCounter {

        public enum MobType {
            CHICKEN("chicken"),
            COW("cow"),
            PIG("pig"),
            SHEEP("sheep"),
            RABBIT("rabbit"),
            BAT("bat"),
            SQUID("squid"),
            MOOSHROOM("mooshroom"),
            VILLAGER("villager"),
            HORSE("horse"),
            DONKEY("donkey"),
            MULE("mule"),
            OCELOT("ocelot"),
            WOLF("wolf"),

            SPIDER("spider"),
            CAVE_SPIDER("cave_spider"),
            ENDERMAN("enderman"),
            PIGMAN("zombie_pigman"),

            ZOMBIE("zombie"),
            SKELETON("skeleton"),
            CREEPER("creeper"),
            SPIDER_JOCKEY("spider_jockey"),
            SLIME("slime"),
            GHAST("ghast"),
            ZOMBIE_VILLAGER("zombie_villager"),
            BLAZE("blaze"),
            WITCH("witch"),
            ENDERMITE("endermite"),
            SILVERFISH("silverfish"),
            GUARDIAN("guardian"),
            ELDER_GUARDIAN("elder_guardian"),
            WITHER_SKELETON("wither_skeleton"),
            WITHER("wither"),
            ENDER_DRAGON("ender_dragon"),
            MAGMA_CUBE("magma_cube"),
            IRON_GOLEM("iron_golem"),
            SNOW_GOLEM("snow_golem");

            private final String name;

            MobType(String name) {
                this.name = name;
            }

            @Override
            public String toString() {
                return name;
            }

            public static MobType fromString(String mobName) {
                return switch (mobName.toLowerCase()) {
                    case "huhn", "chicken" -> CHICKEN;
                    case "kuh", "cow" -> COW;
                    case "schwein", "pig" -> PIG;
                    case "schaf", "sheep" -> SHEEP;
                    case "kaninchen", "rabbit" -> RABBIT;
                    case "fledermaus", "bat" -> BAT;
                    case "tintenfisch", "squid" -> SQUID;
                    case "mooshroom", "pilzkuh" -> MOOSHROOM;
                    case "dorfbewohner", "villager" -> VILLAGER;
                    case "pferd", "horse" -> HORSE;
                    case "esel", "donkey" -> DONKEY;
                    case "mule" -> MULE;
                    case "ocelot" -> OCELOT;
                    case "wolf" -> WOLF;
                    case "spinne", "spider" -> SPIDER;
                    case "höhlenspinne", "cave spider" -> CAVE_SPIDER;
                    case "enderman" -> ENDERMAN;
                    case "zombie" -> ZOMBIE;
                    case "skelett", "skeleton" -> SKELETON;
                    case "creeper" -> CREEPER;
                    case "spider jockey" -> SPIDER_JOCKEY;
                    case "schleim", "slime" -> SLIME;
                    case "ghast" -> GHAST;
                    case "zombie-dorfbewohner", "zombie villager" -> ZOMBIE_VILLAGER;
                    case "blaze" -> BLAZE;
                    case "hexe", "witch" -> WITCH;
                    case "endermite" -> ENDERMITE;
                    case "silberfischchen", "silberfisch", "silverfish" -> SILVERFISH;
                    case "wächter", "guardian" -> GUARDIAN;
                    case "älterer wächter", "elder guardian" -> ELDER_GUARDIAN;
                    case "wither" -> WITHER;
                    case "wither_skeleton" -> WITHER_SKELETON;
                    case "enderdrache", "ender dragon" -> ENDER_DRAGON;
                    case "magmawürfel", "magma cube" -> MAGMA_CUBE;
                    case "eisengolem", "iron golem" -> IRON_GOLEM;
                    case "zombie pigman", "pigman", "pig zombie", "schweinezombie" -> PIGMAN;
                    case "snowman", "snow golem" -> SNOW_GOLEM;
                    default -> null;
                };
            }
        }


        private final Map<MobType, Integer> mobCountMap = new HashMap<>();

        public void addMob(String mobName) {
            int quantity = 1;
            MobType mobType;

            if (mobName.matches("\\d+x\\s+.+")) {
                int splitIndex = mobName.indexOf('x') + 1;
                quantity = Integer.parseInt(mobName.substring(0, splitIndex - 1).trim());
                mobName = mobName.substring(splitIndex).trim();
            }

            mobType = MobType.fromString(mobName);

            if (mobType != null) {
                mobCountMap.put(mobType, mobCountMap.getOrDefault(mobType, 0) + quantity);
            } else {
                Addon.getSharedInstance().logger().warn("Unknown mob type: " + mobName);
            }
        }

        public int getMobCount(MobType mobType) {
            return mobCountMap.getOrDefault(mobType, 0);
        }

        public String getFormattedMobCounts() {
            StringBuilder result = new StringBuilder();
            for (Map.Entry<MobType, Integer> entry : mobCountMap.entrySet()) {
                int count = entry.getValue();
                if (count > 0 && entry.getKey() != null) {
                    String translationKey = "tmbgriefergames.mobs." + entry.getKey().name().toLowerCase();
                    String mobName = I18n.getTranslation(translationKey);

                    result.append("\n§d").append(count).append("x §6").append(mobName);
                }
            }
            return result.toString().trim();
        }
    }

    public static class NearbyWidgetConfig extends TextHudWidgetConfig {

        @SwitchSetting
        private final ConfigProperty<Boolean> showMobs = new ConfigProperty<>(true);

        public ConfigProperty<Boolean> getShowMobs() {return showMobs;}

    }
}
