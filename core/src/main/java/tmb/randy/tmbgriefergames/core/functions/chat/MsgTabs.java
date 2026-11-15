package tmb.randy.tmbgriefergames.core.functions.chat;

import java.util.List;
import java.util.Set;
import net.labymod.api.Laby;
import net.labymod.api.client.chat.filter.ChatFilter;
import net.labymod.api.configuration.labymod.chat.ChatTab;
import net.labymod.api.configuration.labymod.chat.ChatWindow;
import net.labymod.api.configuration.labymod.chat.config.RootChatTabConfig;
import net.labymod.api.configuration.labymod.chat.config.RootChatTabConfig.Type;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.api.enums.CBs;
import tmb.randy.tmbgriefergames.api.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;

/** Opens a chat tab per /msg partner; messages written in such a tab are sent to that player. */
public class MsgTabs extends Function {
    private static final String TAB_PREFIX = "➥ ";
    private static final String OUTGOING_MARKER = "[mir -> ";
    // "7" and "(" are /msg and /r typed with a German keyboard layout without shift
    private static final Set<String> MSG_COMMANDS = Set.of("/msg", "7msg", "(msg");
    private static final Set<String> REPLY_COMMANDS = Set.of("/r", "7r", "(r");

    private static String currentChatPartner = null;
    private static boolean clearedMsgTabs = false;

    public MsgTabs() {
        super(Functions.MSGTABS.name());
    }

    private static boolean isEnabled() {
        return Addon.settings().getChatConfig().getMsgTabMode().get();
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        if (!isEnabled())
            return;

        String message = event.chatMessage().getPlainText();
        currentChatPartner = getChatPartnerName(message);
        if (currentChatPartner == null)
            return;

        getOrCreateTab(message);
        if (Addon.isChatGuiClosed() && message.contains(OUTGOING_MARKER)) {
            Laby.references().chatAccessor().reload();
            Addon.openChat();
        }
    }

    @Override
    public void chatMessageSendEvent(ChatMessageSendEvent event) {
        String message = event.getMessage();
        if (!isEnabled() || message == null || MSG_COMMANDS.stream().anyMatch(message::startsWith))
            return;

        ChatWindow mainWindow = getChatWindow();
        if (mainWindow == null || !mainWindow.getActiveTab().getName().startsWith(TAB_PREFIX))
            return;

        String receiver = mainWindow.getActiveTab().getName().substring(TAB_PREFIX.length());
        if (receiver.length() < 3)
            return;

        String command = message.split(" ")[0].toLowerCase();
        String text;
        if (REPLY_COMMANDS.contains(command)) {
            int firstSpace = message.indexOf(' ');
            if (firstSpace == -1)
                return;
            text = message.substring(firstSpace + 1);
        } else if (command.startsWith("/") || MSG_COMMANDS.contains(command)) {
            return;
        } else {
            text = message;
        }

        event.changeMessage((receiver.equals(currentChatPartner) ? "/r " : "/msg " + receiver + " ") + text);
    }

    @Override
    public void cbChangedEvent(CbChangedEvent event) {
        // The tabs of the last session are empty, so they are removed once per game start
        if (Addon.isGG() && !clearedMsgTabs && event.CB() == CBs.PORTAL && isEnabled()) {
            clearedMsgTabs = true;
            clearAllMsgTabs();
        }
    }

    private void getOrCreateTab(String message) {
        ChatWindow mainWindow = getChatWindow();
        if (mainWindow == null)
            return;

        String tabName = TAB_PREFIX + getChatPartnerName(message);
        for (ChatTab tab : mainWindow.getTabs()) {
            if (tab.getName().equals(tabName))
                return;
        }

        String player = getChatPartnerRank(message) + " ┃ " + getChatPartnerName(message);
        ChatTab tab = mainWindow.initializeTab(new RootChatTabConfig(Type.CUSTOM, tabName));
        List<ChatFilter> filters = tab.config().filters().get();
        filters.add(createFilter("outgoing /msg", OUTGOING_MARKER + player + "]", false));
        filters.add(createFilter("incoming /msg", "[" + player + " -> mir]", true));
        filters.add(createFilter("incoming payment", player + " hat dir $", true));
        filters.add(createFilter("outgoing payment", "Du hast " + player + " $", false));
        filters.add(createFilter("incoming TPA", player + " möchte sich zu dir teleportieren.", true));
        filters.add(createFilter("outgoing TPA", "Anfrage gesendet an " + player, false));
    }

    private static ChatFilter createFilter(String name, String tag, boolean playSound) {
        ChatFilter filter = new ChatFilter();
        filter.name().set(name);
        filter.getIncludedTags().add(tag);
        filter.shouldPlaySound().set(playSound);
        filter.caseSensitive().set(true);
        return filter;
    }

    private void clearAllMsgTabs() {
        ChatWindow mainWindow = getChatWindow();
        if (mainWindow == null)
            return;

        for (ChatTab tab : List.copyOf(mainWindow.getTabs())) {
            if (tab.getName().startsWith(TAB_PREFIX))
                mainWindow.deleteTab(tab);
        }
    }

    private static ChatWindow getChatWindow() {
        if (!isEnabled())
            return null;

        for (ChatWindow window : Laby.references().advancedChatController().getWindows()) {
            if (window.isMainWindow())
                return window;
        }
        return null;
    }

    private static boolean isPrivateMessage(String message) {
        return message.contains("[mir ->") || message.contains("-> mir]");
    }

    /** The word after the rank separator, e.g. "Name" in "[Rang ┃ Name -> mir]". */
    private static String getChatPartnerName(String message) {
        if (!isPrivateMessage(message))
            return null;

        String[] words = message.split(" ");
        for (int i = 0; i < words.length - 1 && words.length >= 5; i++) {
            if (words[i].equals("┃"))
                return words[i + 1].replace("[", "").replace("]", "");
        }
        return null;
    }

    /** The word before the rank separator, e.g. "Rang" in "[Rang ┃ Name -> mir]". */
    private static String getChatPartnerRank(String message) {
        if (!isPrivateMessage(message))
            return null;

        String[] words = message.split(" ");
        for (int i = 1; i < words.length; i++) {
            if (words[i].equals("┃"))
                return words[i - 1].replace("[", "").replace("]", "");
        }
        return null;
    }
}
