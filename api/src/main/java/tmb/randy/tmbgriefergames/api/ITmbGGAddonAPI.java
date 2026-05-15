package tmb.randy.tmbgriefergames.api;

import java.util.List;
import tmb.randy.tmbgriefergames.api.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.api.functions.Function;

public interface ITmbGGAddonAPI {

    void registerFunction(Function function);

    Function getFunction(String identifier);

    ActiveFunction getActiveFunction(String identifier);

    List<Function> getFunctions();

    List<ActiveFunction> getActiveFunctions();
}
