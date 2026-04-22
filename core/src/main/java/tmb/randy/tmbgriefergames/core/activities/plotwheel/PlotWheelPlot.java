package tmb.randy.tmbgriefergames.core.activities.plotwheel;

import org.jetbrains.annotations.NotNull;
import tmb.randy.tmbgriefergames.core.enums.CBs;
import java.util.UUID;

public record PlotWheelPlot(CBs cb, String name, String command, UUID account) {

    @Override
    public @NotNull String toString() {
        return cb.getName() + ";" + name + ";" + command + ";" + account;
    }
}
