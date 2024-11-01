package tmb.randy.tmbgriefergames.core.util;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent.Side;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class ItemClearTimerListener {
    private static int itemRemover = -1;
    private static long endTime = -1L;

    @Subscribe
    public void networkPayloadEvent(NetworkPayloadEvent event) {
        if(event.side() == Side.RECEIVE && Addon.isGG()) {
            byte[] packetBuffer = event.getPayload().clone();
            String payloadString = new String(packetBuffer, StandardCharsets.UTF_8);

            if(payloadString.contains("countdown_create")) {
                itemRemover = extractUntilValue(payloadString);
                TimeUnit timeUnit = TimeUnit.SECONDS;
                if(itemRemover != -1) {
                    endTime = System.currentTimeMillis() + timeUnit.toMillis(itemRemover);
                }
            }
        }
    }

    @Subscribe
    public void CbChanged(CbChangedEvent event) {
        if(!Addon.isGG())
            return;

        itemRemover = -1;
    }

    private int extractUntilValue(String input) {
        String pattern = "until\":(\\d+)";

        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(input);

        if (matcher.find()) {
            String untilValueStr = matcher.group(1);
            return Integer.parseInt(untilValueStr);
        }

        return -1;
    }

    public static String getDisplayValue() {
        long remainingTime = endTime - System.currentTimeMillis();

        if (remainingTime < 0) {
            return "";
        }

        long minutes = remainingTime / (60 * 1000);
        long seconds = (remainingTime % (60 * 1000)) / 1000;

        return (minutes > 0) ? String.format("%02d:%02d", minutes, seconds) : String.format("%d s", seconds);
    }
}
