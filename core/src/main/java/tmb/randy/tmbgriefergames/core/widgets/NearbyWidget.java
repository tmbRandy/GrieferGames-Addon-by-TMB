package tmb.randy.tmbgriefergames.core.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.TextComponent;
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
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.helper.I19n;
import tmb.randy.tmbgriefergames.core.widgets.NearbyWidget.NearbyWidgetConfig;

public class NearbyWidget extends TextHudWidget<NearbyWidgetConfig> {
    private TextComponent name;
    private TextLine line;

    public NearbyWidget(HudWidgetCategory category) {
        super("nearby", NearbyWidgetConfig.class);
        this.name = Component.text(Laby.labyAPI().getName());
        setIcon(Icon.texture(ResourceLocation.create(Addon.getNamespace(), "textures/widgets/nearby.png")));
        this.bindCategory(category);
    }

    @Override
    public void load(NearbyWidgetConfig config) {
        super.load(config);
        this.line = super.createLine(I19n.translate("nearby.nearby"), name);
    }

    @Override
    public void onTick(boolean isEditorContext) {
        if(Addon.isGG()) {
            name = getPlayersListAsString();
            this.line.updateAndFlush(name);
        }
    }

    @Override
    public boolean isVisibleInGame() {
        return Addon.isGG();
    }

    private TextComponent getPlayersListAsString() {
        TextComponent output = Component.empty();
        for (Player player : getPlayersList()) {
            int dist = (int)Math.sqrt(player.getDistanceSquared(Laby.labyAPI().minecraft().getClientPlayer()));
            String distString = " (" + dist + "m)";
            output.append(Component.newline()).append(Component.text(player.getName())).append(Component.text(distString));
        }

        if(config.getShowMobs().get()) {
            MobCounter counter = new MobCounter();
            for (Entity entity : Laby.labyAPI().minecraft().clientWorld().getEntities()) {
                if(entity instanceof Mob mob) {
                    int dist = (int)Math.sqrt(mob.getDistanceSquared(Laby.labyAPI().minecraft().getClientPlayer()));

                    if(dist > 1000)
                        continue;

                    String mobString = mob.toString();
                    String mobName = extractMobInfo(mobString);
                    counter.addMob(mobName);
                }
            }

            if(!counter.mobCountMap.isEmpty()) {
                output.append(Component.newline());
            }

            output.append(Component.text(counter.getFormattedMobCounts()));
        }

        return output;
    }

    public static List<Player> getPlayersList() {
        List<Player> output = new ArrayList<>();

        for (Entity entity : Laby.labyAPI().minecraft().clientWorld().getEntities()) {

            if(entity instanceof Player player) {
                if(Laby.labyAPI().minecraft().getClientPlayer() != null) {
                    if(player.getName().equals(Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).getName()))
                        continue;
                }
                if((player.getName().startsWith("§") && !player.getName().contains("Zauberer")) || player.getName().contains(" "))
                    continue;

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
                    case "ozelot", "ocelot" -> OCELOT;
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
                    case "schneegolem", "snowman", "snow golem" -> SNOW_GOLEM;
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

            if (mobType != null)
                mobCountMap.put(mobType, mobCountMap.getOrDefault(mobType, 0) + quantity);
            /*
                    Uncomment for debugging purposes only as holograms will trigger this and spam the logs
            else {
                //Addon.getSharedInstance().logger().warn("Unknown mob type: " + mobName);
            }
             */
        }

        public String getFormattedMobCounts() {
            StringBuilder result = new StringBuilder();
            for (Map.Entry<MobType, Integer> entry : mobCountMap.entrySet()) {
                int count = entry.getValue();
                if (count > 0 && entry.getKey() != null) {
                    String translationKey = "mobs." + entry.getKey().name().toLowerCase();
                    String mobName = I19n.translate(translationKey);

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
