package tmb.randy.tmbgriefergames.core.enums;

import tmb.randy.tmbgriefergames.core.helper.I19n;

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
    TYPECORRECTION;

    public String getLocalizedName() {return I19n.translate("functions." + name().toLowerCase() + ".name");}
}