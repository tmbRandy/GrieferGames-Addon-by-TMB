package tmb.randy.tmbgriefergames.core;

import net.labymod.api.reference.annotation.Referenceable;

@Referenceable
public interface IBridge {
    void startPlayerTracer(String name);
    void cbChanged();
    boolean isFlyCountdownActive();
    String getWidgetString();
    String getItemRemoverValue();
    void startNewAutocrafter();
    boolean isCompActive();
    void changeSlot(int slot);
    void startAuswurf();
    void startAutocrafterV3();
}
