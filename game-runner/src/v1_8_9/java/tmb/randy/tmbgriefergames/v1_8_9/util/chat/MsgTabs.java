package tmb.randy.tmbgriefergames.v1_8_9.util.chat;

import net.labymod.api.Laby;
import net.labymod.api.client.chat.filter.ChatFilter;
import net.labymod.api.configuration.labymod.chat.ChatTab;
import net.labymod.api.configuration.labymod.chat.ChatWindow;
import net.labymod.api.configuration.labymod.chat.config.RootChatTabConfig;
import net.labymod.api.configuration.labymod.chat.config.RootChatTabConfig.Type;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.core.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.v1_8_9.util.VersionisedBridge;

public class MsgTabs {

    private static String currentChatPartner = null;

    public void chatMessageReceived(ChatReceiveEvent event) {
        if(!Addon.getSharedInstance().configuration().getChatConfig().getMsgTabMode().get())
            return;

        currentChatPartner = getChatPartnerName(event.chatMessage().getPlainText());
        if(currentChatPartner != null) {
            getTabForName(event.chatMessage().getPlainText());

            if(!VersionisedBridge.isChatGuiOpen() && event.chatMessage().getPlainText().contains("[mir -> ")) {
                reloadChat();
                Minecraft.getMinecraft().displayGuiScreen(new GuiChat());
            }
        }
    }

    public void messageSend(ChatMessageSendEvent event) {
        if(!Addon.getSharedInstance().configuration().getChatConfig().getMsgTabMode().get() || event.getMessage().startsWith("/msg") || event.getMessage().startsWith("7msg") || event.getMessage().startsWith("(msg"))
            return;

        ChatWindow mainWindow = getChatWindow();
        if(mainWindow != null) {
            if(mainWindow.getActiveTab().getName().startsWith("➥ ")) {
                String currentReciever = mainWindow.getActiveTab().getName().replace("➥ ", "");
                if(currentReciever.length() >= 3) {
                    String[] messageSplit = event.getMessage().split(" ");
                    if(messageSplit[0].startsWith("/") && !messageSplit[0].equalsIgnoreCase("/msg") && !messageSplit[0].equalsIgnoreCase("7msg") && !messageSplit[0].equalsIgnoreCase("(msg")
                        && !messageSplit[0].equalsIgnoreCase("/r") && !messageSplit[0].equalsIgnoreCase("7r") && !messageSplit[0].equalsIgnoreCase("(r")) {
                        return;
                    }
                    if(messageSplit[0].equalsIgnoreCase("/r") || messageSplit[0].equalsIgnoreCase("7r") || messageSplit[0].equalsIgnoreCase("(r")) {
                        int firstSpaceIndex = event.getMessage().indexOf(" ");
                        if (firstSpaceIndex != -1) {
                            if(currentReciever.equals(currentChatPartner)) {
                                event.changeMessage("/r " + event.getMessage().substring(firstSpaceIndex + 1));
                            } else {
                                event.changeMessage(mainWindow.getActiveTab().getName().replace("➥", "/msg") + " " + event.getMessage().substring(firstSpaceIndex + 1));
                            }
                        }
                    } else if(!(messageSplit[0].equalsIgnoreCase("/msg") || messageSplit[0].equalsIgnoreCase("7msg") || messageSplit[0].equalsIgnoreCase("(msg"))) {
                        if(currentReciever.equals(currentChatPartner)) {
                            event.changeMessage("/r " + event.getMessage());
                        } else {
                            event.changeMessage(mainWindow.getActiveTab().getName().replace("➥", "/msg") + " " + event.getMessage());
                        }
                    }
                }
            }
        }
    }

    public ChatTab createTabForPlayer(String message) {
        ChatWindow mainWindow = getChatWindow();
        if(mainWindow != null) {

            String name = getChatPartnerName(message);
            String rank = getChatPartnerRank(message);

            String incomingFilter = "[" + rank + " ┃ " + name + " -> mir]";
            String outgoingFilter = "[mir -> " + rank + " ┃ " + name + "]";

            RootChatTabConfig tabConfig = new RootChatTabConfig(Type.CUSTOM, "➥ " + name);
            ChatTab tab = mainWindow.initializeTab(tabConfig);

            ChatFilter filterOutgoingMsg = new ChatFilter();
            filterOutgoingMsg.name().set("outgoing /msg");
            filterOutgoingMsg.getIncludedTags().add(outgoingFilter);
            filterOutgoingMsg.shouldPlaySound().set(false);
            filterOutgoingMsg.caseSensitive().set(true);

            ChatFilter filterIncomingMsg = new ChatFilter();
            filterIncomingMsg.name().set("incoming /msg");
            filterIncomingMsg.getIncludedTags().add(incomingFilter);
            filterIncomingMsg.shouldPlaySound().set(true);
            filterIncomingMsg.caseSensitive().set(true);

            tab.config().filters().get().add(filterOutgoingMsg);
            tab.config().filters().get().add(filterIncomingMsg);

            return tab;
        }
        return null;
    }

    private ChatTab getTabForName(String message) {
        String chatPartner = getChatPartnerName(message);
        ChatTab output = null;

        ChatWindow mainWindow = getChatWindow();
        if(mainWindow != null) {
            for (ChatTab tab : mainWindow.getTabs()) {
                if(tab.getName().equals("➥ " + chatPartner)) {
                    output = tab;
                    break;
                }
            }

            if(output == null) {
                output = createTabForPlayer(message);
            }
        }

        return output;
    }

    private ChatWindow getChatWindow() {
        if(Addon.getSharedInstance().configuration().getChatConfig().getMsgTabMode().get()) {
            for (ChatWindow window : LabyMod.references().advancedChatController().getWindows()) {
                if (window.isMainWindow()) {
                    return window;
                }
            }
        }
        return null;
    }

    private String getChatPartnerName(String message) {
        if(message.contains("[mir ->") || message.contains("-> mir]")) {
            String[] split = message.split(" ");
            if(split.length >= 5) {
                boolean returnNext = false;
                for (String string : split) {
                    if(returnNext)
                        return string.replace("[", "").replace("]", "");
                    else if(string.equals("┃"))
                        returnNext = true;
                }
            }
        }

        return null;
    }

    private String getChatPartnerRank(String message) {
        if(message.contains("[mir ->") || message.contains("-> mir]")) {
            String[] split = message.split(" ");
            String previous = "";

            for (String string : split) {
                if(string.equals("┃")) {
                    return previous.replace("[", "").replace("]", "");
                } else {
                    previous = string;
                }
            }
        }

        return null;
    }

    public static void resetCurrentChatPartner() {
        currentChatPartner = null;
    }

    private void reloadChat() {
        Laby.references().chatAccessor().reload();
    }
}
