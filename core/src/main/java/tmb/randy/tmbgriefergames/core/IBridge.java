package tmb.randy.tmbgriefergames.core;

import net.labymod.api.reference.annotation.Referenceable;

@Referenceable
public interface IBridge {
    public void startPlayerTracer(String name);
    public void cbChanged();
    public boolean isFlyCountdownActive();
    public String getWidgetString();
    public String getItemRemoverValue();
    public void startNewAutocrafter();
    boolean isCompActive();
}
