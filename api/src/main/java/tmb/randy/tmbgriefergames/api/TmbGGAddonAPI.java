package tmb.randy.tmbgriefergames.api;

import java.util.List;
import tmb.randy.tmbgriefergames.api.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.api.functions.Function;

public final class TmbGGAddonAPI {

    private static ITmbGGAddonAPI provider;

    private TmbGGAddonAPI() {}

    public static void setProvider(ITmbGGAddonAPI provider) {
        TmbGGAddonAPI.provider = provider;
    }

    public static void registerFunction(Function function) {
        provider.registerFunction(function);
    }

    public static Function getFunction(String identifier) {
        return provider.getFunction(identifier);
    }

    public static ActiveFunction getActiveFunction(String identifier) {
        return provider.getActiveFunction(identifier);
    }

    public static List<Function> getFunctions() {
        return provider.getFunctions();
    }

    public static List<ActiveFunction> getActiveFunctions() {
        return provider.getActiveFunctions();
    }
}
