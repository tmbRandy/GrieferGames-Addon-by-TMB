package tmb.randy.tmbgriefergames.core.functions.chat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.config.ChatSubConfig;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.core.helper.Commander;

/** Corrects typical command typos and splits long private messages into several messages. */
public class TypeCorrection extends Function {
    private static final int MAX_MESSAGE_LENGTH = 100;
    private static final int SPLIT_LENGTH = 97;

    /** Commands that are often mistyped as "7cmd" / "(cmd" (missing shift) or "t/cmd" (chat key pressed twice). */
    private static final List<String> COMMANDS = List.of("r", "ec", "msg", "pay", "bank", "craft", "invsee", "booster", "cooldowns", "p h ", "tpaccept", "tpahere");
    private static final Map<String, String> REPLACEMENTS = createReplacements();

    public TypeCorrection() {
        super(Functions.TYPECORRECTION.name());
    }

    private static Map<String, String> createReplacements() {
        Map<String, String> replacements = new LinkedHashMap<>();
        for (String command : COMMANDS) {
            for (String typo : new String[] {"7", "(", "t/"})
                replacements.put(typo + command, "/" + command);
        }

        replacements.put("/p t ", "/p trust ");

        // CB shortcuts
        for (int cb = 22; cb >= 1; cb--)
            replacements.put("/cb" + cb, "/switch cb" + cb);
        replacements.put("/nature", "/switch nature");
        replacements.put("/evil", "/switch cbevil");
        replacements.put("/extreme", "/switch extreme");
        replacements.put("/lava", "/switch lava");
        replacements.put("/wasser", "/switch wasser");
        return replacements;
    }

    @Override
    public void chatMessageSendEvent(ChatMessageSendEvent event) {
        ChatSubConfig config = Addon.settings().getChatConfig();
        String message = event.getMessage();
        if (message == null || (!config.getTypeCorrection().get() && !config.getMessageSplit().get()))
            return;

        if (config.getTypeCorrection().get()) {
            for (Map.Entry<String, String> entry : REPLACEMENTS.entrySet())
                message = message.replace(entry.getKey(), entry.getValue());
        }

        String lowerCase = message.toLowerCase();
        if (config.getMessageSplit().get() && message.length() > MAX_MESSAGE_LENGTH && (lowerCase.startsWith("/msg ") || lowerCase.startsWith("/r "))) {
            String[] parts = message.split("(?<=\\G.{" + SPLIT_LENGTH + "})");
            for (int i = 0; i < parts.length; i++)
                Commander.queue(i == 0 ? parts[i] : "/r " + parts[i]);

            event.setCancelled(true);
            event.changeMessage("");
            return;
        }

        if (!event.isCancelled())
            event.changeMessage(message);
        else
            event.changeMessage("");
    }
}
