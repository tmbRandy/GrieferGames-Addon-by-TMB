package tmb.randy.tmbgriefergames.core.flow.node.nodes;

import static tmb.randy.tmbgriefergames.core.flow.node.NodeType.NEXT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.FOUND;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.NONE;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.number;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.text;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import net.labymod.api.Laby;
import net.labymod.api.client.component.serializer.plain.PlainTextComponentSerializer;
import net.labymod.api.client.network.ClientPacketListener;
import net.labymod.api.client.network.NetworkPlayerInfo;
import tmb.randy.tmbgriefergames.core.flow.node.NodeContext;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType.Category;
import tmb.randy.tmbgriefergames.core.flow.node.NodeTypes;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;

/**
 * Variables. Every text parameter of every block can use ${name} to insert a variable; these blocks write them.
 * Blocks that store several values use the name as prefix, e.g. "chest" becomes ${chest.x}, ${chest.y}, ${chest.z}.
 */
public final class DataNodes {

    private DataNodes() {}

    /** The rank in front of the own name in the tab list ("Rang ┃ Name"), empty if unknown. */
    private static String getOwnRank() {
        ClientPacketListener connection = Laby.labyAPI().minecraft().getClientPacketListener();
        NetworkPlayerInfo info = connection == null ? null : connection.getNetworkPlayerInfo(Laby.labyAPI().getName());
        if (info == null || info.displayName() == null)
            return "";

        String[] parts = PlainTextComponentSerializer.plainText().serialize(info.displayName()).split("┃");
        return parts.length > 1 ? parts[0].trim() : "";
    }

    public static void register() {
        NodeTypes.register("set_variable", Category.DATA, List.of(text("name", "counter"), text("value", "0")), List.of(NEXT), true, context -> {
            context.setVariable(context.rawText("name"), context.text("value"));
            return NEXT;
        });

        NodeTypes.register("calculate", Category.DATA, List.of(text("name", "counter"), text("operator", "+"), text("value", "1")), List.of(NEXT), true, context -> {
            int current = NodeContext.toNumber(context.variable(context.rawText("name")));
            int value = context.number("value");
            int result = switch (context.text("operator").trim()) {
                case "-" -> current - value;
                case "*" -> current * value;
                case "/" -> value == 0 ? current : current / value;
                case "%" -> value == 0 ? current : current % value;
                case "min" -> Math.min(current, value);
                case "max" -> Math.max(current, value);
                case "=" -> value;
                default -> current + value;
            };
            context.setVariable(context.rawText("name"), result);
            return NEXT;
        });

        NodeTypes.register("store_looked_block", Category.DATA, List.of(text("name", "block")), List.of(FOUND, NONE), true, context -> {
            int[] pos = context.game().getBlockLookingAt();
            if (pos == null)
                return NONE;

            String name = context.rawText("name");
            context.setVariable(name + ".x", pos[0]);
            context.setVariable(name + ".y", pos[1]);
            context.setVariable(name + ".z", pos[2]);
            context.setVariable(name + ".id", context.game().getBlockAt(pos[0], pos[1], pos[2]));
            return FOUND;
        });

        NodeTypes.register("store_slot", Category.DATA, List.of(number("slot", 0), text("name", "item")), List.of(FOUND, NONE), true, context -> {
            int slot = context.number("slot");
            String item = context.game().getSlotItem(slot);
            String name = context.rawText("name");
            context.setVariable(name + ".id", item == null ? "" : item);
            context.setVariable(name + ".name", item == null ? "" : NodeTypes.stripColors(context.game().getSlotItemName(slot)));
            context.setVariable(name + ".count", context.game().getSlotCount(slot));
            context.setVariable(name + ".level", context.game().getSlotCompressionLevel(slot));
            return item == null ? NONE : FOUND;
        });

        NodeTypes.register("store_position", Category.DATA, List.of(text("name", "position")), List.of(NEXT), true, context -> {
            double[] position = context.game().getPlayerPosition();
            String name = context.rawText("name");
            context.setVariable(name + ".x", (int) Math.floor(position[0]));
            context.setVariable(name + ".y", (int) Math.floor(position[1]));
            context.setVariable(name + ".z", (int) Math.floor(position[2]));
            return NEXT;
        });

        // Cuts out the text between two markers, e.g. the date of "Du kannst erst am <date> wieder ..."
        NodeTypes.register("extract_text", Category.DATA, List.of(text("text", "${event}"), text("after", ""), text("before", ""), text("name", "text")),
            List.of(FOUND, NONE), true, context -> {
                String text = context.text("text");
                String after = context.text("after");
                String before = context.text("before");
                int start = after.isEmpty() ? 0 : text.indexOf(after);
                if (start < 0)
                    return NONE;
                start += after.length();
                int end = before.isEmpty() ? text.length() : text.indexOf(before, start);
                if (end < 0)
                    return NONE;
                context.setVariable(context.rawText("name"), text.substring(start, end).trim());
                return FOUND;
            });

        // Splits e.g. "/p h Name 3" into ${part.1}="/p", ${part.2}="h", ... and ${part.count}
        NodeTypes.register("split_text", Category.DATA, List.of(text("text", "${event}"), text("separator", " "), text("name", "part")), List.of(NEXT), true, context -> {
            String separator = context.text("separator").isEmpty() ? " " : context.text("separator");
            String[] parts = context.text("text").split(Pattern.quote(separator), -1);
            String name = context.rawText("name");
            for (int i = 0; i < parts.length; i++)
                context.setVariable(name + "." + (i + 1), parts[i].trim());
            context.setVariable(name + ".count", parts.length);
            return NEXT;
        });

        // Turns a date like "31.10.2026 um 13:30:00" into seconds, comparable with ${time.now}
        NodeTypes.register("parse_date", Category.DATA, List.of(text("text", ""), text("format", "dd.MM.yyyy 'um' HH:mm:ss"), text("name", "date")),
            List.of(FOUND, NONE), true, context -> {
                try {
                    LocalDateTime date = LocalDateTime.parse(context.text("text").trim(), DateTimeFormatter.ofPattern(context.text("format")));
                    context.setVariable(context.rawText("name"), date.atZone(ZoneId.systemDefault()).toEpochSecond());
                    return FOUND;
                } catch (RuntimeException e) {
                    return NONE;
                }
            });

        NodeTypes.register("store_info", Category.DATA, List.of(text("name", "info")), List.of(NEXT), true, context -> {
            String name = context.rawText("name");
            String title = context.game().getChestTitle();
            context.setVariable(name + ".menu", title == null ? "" : NodeTypes.stripColors(title));
            context.setVariable(name + ".screen", context.game().getScreenType());
            context.setVariable(name + ".cb", CBtracker.getCurrentCB().getName());
            context.setVariable(name + ".hotbar", context.game().getHotbarSlot() + 1);
            String held = context.game().getHeldItem();
            context.setVariable(name + ".held", held == null ? "" : held);
            context.setVariable(name + ".durability", context.game().getHeldItemDurability());
            context.setVariable(name + ".rank", getOwnRank());
            context.setVariable(name + ".player", Laby.labyAPI().getName());
            return NEXT;
        });
    }
}
