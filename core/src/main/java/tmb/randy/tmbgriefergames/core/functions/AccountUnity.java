package tmb.randy.tmbgriefergames.core.functions;

import java.util.Collection;
import java.util.Objects;
import net.labymod.accountmanager.storage.account.Account;
import net.labymod.api.Laby;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.scoreboard.Scoreboard;
import net.labymod.api.client.scoreboard.ScoreboardTeam;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.scoreboard.ScoreboardTeamEntryAddEvent;
import net.labymod.api.mojang.GameProfile;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.core.helper.Commander;

public class AccountUnity extends Function {

    private int counter = 0;

    public AccountUnity() {
        super(Functions.ACCOUNTUNITY.name());
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        counter++;

        if (counter == 50)
            payToMainAccount();

        if (counter > 300)
            counter = 0;
    }

    @Override
    public void scoreboardTeamEntryAddEvent(ScoreboardTeamEntryAddEvent event) {
        payToMainAccount();
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        String message = event.chatMessage().getPlainText();

        if (Addon.settings().getAccountUnitySubConfig().getTpAccept().get()
            && (message.endsWith(" möchte sich zu dir teleportieren.") || message.endsWith(
            " möchte, dass du dich zu der Person teleportierst."))) {
            for (Account account : Laby.labyAPI().getAccountManager().getAccounts()) {
                String name = account.getUsername();

                if (message.endsWith(name + " möchte sich zu dir teleportieren.")
                    || message.endsWith(
                    name + " möchte, dass du dich zu der Person teleportierst.")) {
                    Laby.references().chatExecutor().chat("/tpaccept");
                }
            }
        } else if (
            Addon.settings().getAccountUnitySubConfig().getVoteBooster()
                .get() && message.startsWith("[StartKick] Ersteller: ") || message.startsWith(
                "[StartJail] Ersteller: ")) {
            String[] split = message.split(" ");
            String name = split[split.length - 1];

            for (Account account : Laby.labyAPI().getAccountManager().getAccounts()) {
                String accName = account.getUsername();
                if (name.equals(accName)) {
                    Laby.labyAPI().minecraft().chatExecutor().chat("/ja");
                }
            }
        }
    }

    private void payToMainAccount() {
        if (Laby.labyAPI().minecraft().clientWorld() != null
            && Laby.labyAPI().minecraft().getClientPlayer() != null) {
            if (Laby.labyAPI().getAccountManager().getAccounts().length > 1
                && !Addon.settings().getAccountUnitySubConfig()
                .getMainAccount().get().isEmpty()
                && !isCurrentAccountMainAccount()
                && getBalance() > 0f
                && CBtracker.isCommandAbleCB()) {
                String mainAccountPlayerName = Addon.settings()
                    .getAccountUnitySubConfig().getMainAccount().get();
                if (mainAccountPlayerName != null) {
                    if (!mainAccountPlayerName.equals(
                        Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer())
                            .getName())) {
                        if (isPlayerOnCB(mainAccountPlayerName)) {
                            Commander.queue("/pay " + mainAccountPlayerName + " " + getBalance());
                        }
                    }
                }
            }
        }
    }

    private boolean isCurrentAccountMainAccount() {
        GameProfile currentPlayerProfile = Objects.requireNonNull(
            Laby.labyAPI().minecraft().getClientPlayer()).profile();
        String currentName = currentPlayerProfile.getUsername();
        return currentName.equals(
            Addon.settings().getAccountUnitySubConfig().getMainAccount()
                .get());
    }

    private float getBalance() {
        Scoreboard scoreboard = Laby.labyAPI().minecraft().getScoreboard();

        if (scoreboard == null)
            return 0f;

        for (ScoreboardTeam team : scoreboard.getTeams()) {
            if (team.getTeamName().equals("money_value")) {
                try {
                    return Float.parseFloat(
                        ((TextComponent) team.getPrefix()).getText().replace("$", "")
                            .replace(".", "").replace(",", "."));
                } catch (Exception e) {
                    return 0f;
                }

            }
        }
        return 0f;
    }

    private boolean isPlayerOnCB(String name) {
        if (name == null) {
            return false;
        }

        if (Laby.labyAPI().minecraft().getScoreboard() instanceof Scoreboard scoreboard) {
            for (ScoreboardTeam team : scoreboard.getTeams()) {
                Collection<String> members = team.getEntries();

                for (String member : members) {
                    if (member.equalsIgnoreCase(name)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}