package tmb.randy.tmbgriefergames.api.events;

import net.labymod.api.event.Event;

public class MoneyBalanceChangedEvent implements Event {
    private final double oldBalance;
    private final double newBalance;

    public MoneyBalanceChangedEvent(double oldBalance, double newBalance) {
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
    }

    public double getOldBalance() {
        return oldBalance;
    }

    public double getNewBalance() {
        return newBalance;
    }
}
