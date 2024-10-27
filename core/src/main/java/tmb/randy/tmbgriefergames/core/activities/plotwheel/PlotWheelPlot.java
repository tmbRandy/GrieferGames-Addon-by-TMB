package tmb.randy.tmbgriefergames.core.activities.plotwheel;

import tmb.randy.tmbgriefergames.core.enums.CBs;
import java.util.UUID;

public record PlotWheelPlot(CBs cb, String name, String command, UUID account) {

    @Override
    public String toString() {
        return cb.getName() + ";" + name + ";" + command + ";" + account;
    }
}
