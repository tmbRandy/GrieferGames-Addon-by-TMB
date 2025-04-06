package tmb.randy.tmbgriefergames.core.functions.chat;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.core.helper.I19n;


public class CooldownNotifier extends Function {

    private final List<Cooldown> cooldowns = Arrays.asList(
        new Cooldown("/wand", 30000, "chat.cooldownWand"),
        new Cooldown("/rand", 30000, "chat.cooldownRand"),
        new Cooldown("/boden", 30000, "chat.cooldownBoden"),
        new Cooldown("^/sign.*", 3000, "chat.cooldownSign"),
        new Cooldown("/anticopy", 5000, "chat.cooldownAnticopy")
    );

    public CooldownNotifier() {
        super(Functions.COOLDOWNNOTIFIER);
    }

    @Override
    public void chatMessageSendEvent(ChatMessageSendEvent event) {
        if(!Addon.getSharedInstance().configuration().getChatConfig().getCooldownNotifier().get())
            return;

        String str = event.getMessage().toLowerCase();

        for (Cooldown cooldown : cooldowns) {
            if(str.matches(cooldown.command())) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Addon.getSharedInstance().displayNotification("§f" + I19n.translate(cooldown.notificationKey()));
                    }
                }, cooldown.cooldown());
            }
        }
    }

    private record Cooldown(String command, long cooldown, String notificationKey) {}
}
