package tmb.randy.tmbgriefergames.core.functions.chat;

import java.util.List;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.core.helper.BaseHelper;

/** Notifies the player as soon as the cooldown of a plot command has expired. */
public class CooldownNotifier extends Function {

    private record Cooldown(String commandRegex, long durationMs, String notificationKey) {}

    private static final List<Cooldown> COOLDOWNS = List.of(
        new Cooldown("/wand", 30000, "chat.cooldownWand"),
        new Cooldown("/rand", 30000, "chat.cooldownRand"),
        new Cooldown("/boden", 30000, "chat.cooldownBoden"),
        new Cooldown("^/sign.*", 3000, "chat.cooldownSign"),
        new Cooldown("/anticopy", 5000, "chat.cooldownAnticopy")
    );

    public CooldownNotifier() {
        super(Functions.COOLDOWNNOTIFIER.name());
    }

    @Override
    public void chatMessageSendEvent(ChatMessageSendEvent event) {
        if (!Addon.settings().getChatConfig().getCooldownNotifier().get() || event.getMessage() == null)
            return;

        String message = event.getMessage().toLowerCase();
        for (Cooldown cooldown : COOLDOWNS) {
            if (message.matches(cooldown.commandRegex()))
                BaseHelper.schedule(() -> Addon.displayNotification("§f" + Addon.translate(cooldown.notificationKey())), cooldown.durationMs());
        }
    }
}
