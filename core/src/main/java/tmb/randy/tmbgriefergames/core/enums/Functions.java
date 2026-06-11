package tmb.randy.tmbgriefergames.core.enums;

import tmb.randy.tmbgriefergames.core.Addon;

public enum Functions {
    PLAYERTRACER,
    HABK,
    VABK,
    COMP,
    DECOMP,
    CRAFTV1,
    CRAFTV2,
    CRAFTV3,
    EJECT,
    PLOTSWITCH,
    AUTOHOPPER,
    AUTOLOOT,
    BLOCKMARKER,
    POTIONTIMER,
    HOPPERCONNECTIONS,
    ITEMSHIFTER,
    NATUREBORDERSRENDERER,
    ACCOUNTUNITY,
    ITEMSAVER,
    TOOLTIPEXTENSION,
    CHATCLEANER,
    COOLDOWNNOTIFIER,
    MSGTABS,
    PAYMENTVALIDATOR,
    TYPECORRECTION,
    AUTOFISHER,
    INFINITYMINER;

    public String getLocalizedName() {return Addon.translate("functions." + name().toLowerCase() + ".name");}
}