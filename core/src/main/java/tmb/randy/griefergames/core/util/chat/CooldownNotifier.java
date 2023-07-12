package tmb.randy.griefergames.core.util.chat;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import net.labymod.api.util.I18n;
import tmb.randy.griefergames.core.Addon;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class CooldownNotifier {

    private final List<Cooldown> cooldowns = Arrays.asList(
        new Cooldown("/wand", 30000, "griefergames.chat.cooldownWand"),
        new Cooldown("/rand", 30000, "griefergames.chat.cooldownRand"),
        new Cooldown("/boden", 30000, "griefergames.chat.cooldownBoden"),
        new Cooldown("^/sign.*", 3000, "griefergames.chat.cooldownSign"),
        new Cooldown("/anticopy", 5000, "griefergames.chat.cooldownAnticopy")
    );

    @Subscribe
    public void messageReceived(ChatMessageSendEvent event) {
        if(!Addon.isGG() || !Addon.getSharedInstance().configuration().getChatConfig().getCooldownNotifier().get()) {
            return;
        }

        String str = event.getMessage().toLowerCase();

        for (Cooldown cooldown : cooldowns) {
            if(str.matches(cooldown.getCommand())) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            Addon.getSharedInstance().displayNotification("§f" + I18n.translate(cooldown.getNotificationKey()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, cooldown.getCooldown());
            }
        }
    }

    private static class Cooldown {
        private final String command;
        private final long cooldown;
        private final String notificationKey;
        Cooldown(String command, long cooldown, String notificationKey) {
            this.command = command;
            this.cooldown = cooldown;
            this.notificationKey = notificationKey;
        }

        public String getCommand() {return command;}
        public long getCooldown() {return cooldown;}
        public String getNotificationKey() {return notificationKey;}
    }
}
