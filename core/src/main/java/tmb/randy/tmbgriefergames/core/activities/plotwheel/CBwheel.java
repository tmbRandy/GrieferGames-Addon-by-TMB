package tmb.randy.tmbgriefergames.core.activities.plotwheel;

import net.labymod.api.Laby;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.WheelWidget;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.api.enums.CBs;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;

@AutoWidget
@Link("cbwheel.lss")
public class CBwheel extends WheelWidget {

    public void initialize(Parent parent) {
        this.refresh();
        super.initialize(parent);
    }

    public void refresh() {
        this.removeChildIf((widget) -> widget instanceof Segment);

        CBs[] cbs = CBs.values();
        for (int i = cbs.length - 1; i >= 0; i--) {
            CBs cb = cbs[i];
            if (cb == CBs.EVENT || !CBtracker.isPlotworldCB(cb)) continue;

            CBsegment segment = new CBsegment(cb);
            segment.addId("segment-" + cb, "cb-segment");
            this.addSegment(segment);

            if (CBtracker.getCurrentCB() == cb)
                segment.setSelected(true);
        }
    }

    public static class CBsegment extends Segment {
        private final CBs cb;

        public CBsegment(CBs cb) {
            this.cb = cb;
            ComponentWidget display = ComponentWidget.text(cb.getName());

            display.addId("cb-segment-display");
            this.addChild(display);

            this.setPressable(() -> {
                if(cb != CBtracker.getCurrentCB()) {
                    Laby.references().chatExecutor().chat(Const.Cmd.SWITCH + cb);
                }
            });
        }

        public CBs getCb() {
            return cb;
        }
    }
}