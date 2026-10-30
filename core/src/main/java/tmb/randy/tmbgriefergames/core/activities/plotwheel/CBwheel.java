package tmb.randy.tmbgriefergames.core.activities.plotwheel;

import java.util.List;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.WheelWidget;
import tmb.randy.tmbgriefergames.api.enums.CBs;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;

/** Outer wheel of the plot wheel: one segment per CB, clicking switches to that CB. */
@AutoWidget
@Link("cbwheel.lss")
public class CBwheel extends WheelWidget {

    @Override
    public void initialize(Parent parent) {
        refresh();
        super.initialize(parent);
    }

    public void refresh() {
        removeChildIf(widget -> widget instanceof Segment);

        List<CBs> cbs = CBtracker.getSwitchablePlotworldCBs();
        for (int i = cbs.size() - 1; i >= 0; i--) {
            CBsegment segment = new CBsegment(cbs.get(i));
            addSegment(segment);
            if (cbs.get(i) == CBtracker.getCurrentCB())
                segment.setSelected(true);
        }
    }

    public static class CBsegment extends Segment {
        private final CBs cb;

        public CBsegment(CBs cb) {
            this.cb = cb;
            addId("segment-" + cb, "cb-segment");
            addChild(ComponentWidget.text(cb.getName()).addId("cb-segment-display"));

            setPressable(() -> {
                if (cb != CBtracker.getCurrentCB())
                    Laby.references().chatExecutor().chat(Const.Cmd.SWITCH + cb);
            });
        }

        public CBs getCb() {
            return cb;
        }
    }
}
