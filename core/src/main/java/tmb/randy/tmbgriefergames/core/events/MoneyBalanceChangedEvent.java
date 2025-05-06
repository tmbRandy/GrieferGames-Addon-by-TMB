package tmb.randy.tmbgriefergames.core.events;

import net.labymod.api.event.Event;

public class MoneyBalanceChangedEvent implements Event {
    double oldBalance;
    double newBalance;

    public MoneyBalanceChangedEvent(double oldBalance, double newBalance) {
        this.newBalance = newBalance;
        this.oldBalance = oldBalance;
    }

    public double getOldBalance() {
        return oldBalance;
    }

    public double getNewBalance() {
        return newBalance;
    }
}