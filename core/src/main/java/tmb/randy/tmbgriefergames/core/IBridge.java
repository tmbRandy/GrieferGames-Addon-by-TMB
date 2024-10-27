package tmb.randy.tmbgriefergames.core;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.reference.annotation.Referenceable;

@Referenceable
public interface IBridge {
    boolean isFlyCountdownActive();
    String getWidgetString();
    void startNewAutocrafter();
    boolean isCompActive();
    void changeSlot(int slot);
    void startAuswurf();
    void startAutocrafterV3();
    void openChat();
    boolean isChatGuiClosed();
    void resetLines();
    boolean allKeysPressed(Key[] keys);
}
