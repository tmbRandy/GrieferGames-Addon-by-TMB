package tmb.randy.tmbgriefergames.core.events;

import net.labymod.api.event.Event;
import java.math.BigDecimal;

public class MoneyBalanceChangedEvent implements Event {
    BigDecimal oldBalance;
    BigDecimal newBalance;

    public MoneyBalanceChangedEvent(BigDecimal oldBalance, BigDecimal newBalance) {
        this.newBalance = newBalance;
        this.oldBalance = oldBalance;
    }

    public BigDecimal getOldBalance() {
        return oldBalance;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }
}