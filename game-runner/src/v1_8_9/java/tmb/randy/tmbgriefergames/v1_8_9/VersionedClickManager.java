package tmb.randy.tmbgriefergames.v1_8_9;

import tmb.randy.tmbgriefergames.core.click.Click;
import tmb.randy.tmbgriefergames.core.click.ClickManager;

public class VersionedClickManager extends ClickManager {

    @Override
    protected int getWindowId() {
        return Helper.getWindowId();
    }

    @Override
    protected boolean isClickableScreen() {
        return Helper.isClickableScreen();
    }

    @Override
    protected void sendClick(Click click) {
        Helper.windowClick(click);
    }
}
