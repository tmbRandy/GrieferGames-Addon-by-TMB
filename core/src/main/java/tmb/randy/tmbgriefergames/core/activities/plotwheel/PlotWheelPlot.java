package tmb.randy.tmbgriefergames.core.activities.plotwheel;

import tmb.randy.tmbgriefergames.api.enums.CBs;
import java.util.UUID;

public record PlotWheelPlot(CBs cb, String name, String command, UUID account) {}
