package tmb.randy.tmbgriefergames.v1_12_2.util;

import net.labymod.api.Laby;
import net.labymod.api.event.Phase;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import tmb.randy.tmbgriefergames.core.Addon;

public class PlotSwitch {

    private static final int COMMAND_COOLDOWN = 25;
    private static int COMMAND_COOLDOWN_COUNTER = 0;
    private String nextCommand = null;

    private boolean waitingForPlotSwitch = false;
    private enum DIRECTION {
        PREVIOUS, NEXT
    }

    private String lastPlot = null;


    public void messageSend(ChatMessageSendEvent event) {
        if(!Addon.isGG()) {
            return;
        }

        if(event.getMessage().toLowerCase().startsWith("/p h")) {
            lastPlot = event.getMessage();
        }
    }


    public void tick(GameTickEvent event) {
        if(event.phase() == Phase.PRE) {
            if(COMMAND_COOLDOWN_COUNTER > 0) {
                COMMAND_COOLDOWN_COUNTER--;
                Addon.getSharedInstance().logger().info("Counting: " + COMMAND_COOLDOWN_COUNTER);
                return;
            }

            if(nextCommand != null && Laby.labyAPI().minecraft().getClientPlayer() != null) {
                COMMAND_COOLDOWN_COUNTER = COMMAND_COOLDOWN;
                waitingForPlotSwitch = true;
                Laby.labyAPI().minecraft().chatExecutor().chat(nextCommand);
                nextCommand = null;
            }
        }
    }


    public void messageReceived(ChatReceiveEvent event) {
        String message = event.chatMessage().getPlainText();

        if(message.equals("[GrieferGames] Du wurdest zum Grundstück teleportiert.") && waitingForPlotSwitch) {
            waitingForPlotSwitch = false;
        }
    }

    public void keyDown(KeyEvent event) {
        if(lastPlot == null || nextCommand != null || !Addon.isGG()) {
            return;
        }

        String command = null;

        if(event.state() == State.PRESS) {
            if(VersionisedBridge.allKeysPressed(Addon.getSharedInstance().configuration().getPreviousPlot().get())) {
                command = getPlotCommand(lastPlot, DIRECTION.PREVIOUS);
            } else if(VersionisedBridge.allKeysPressed(Addon.getSharedInstance().configuration().getNextPlot().get())) {
                command = getPlotCommand(lastPlot, DIRECTION.NEXT);
            }

            if(command != null) {
                nextCommand = command;
            }
        }
    }

    private String getPlotCommand(String command, DIRECTION direction) {
        String[] args = command.split(" ");

        if(args.length == 2) {
            if(!(args[0].equalsIgnoreCase("/p") && args[1].equalsIgnoreCase("h"))) {
                return null;
            }

            if(direction == DIRECTION.PREVIOUS) {
                return null;
            } else {
                return command + " 2";
            }
        } else if(args.length == 3) {
            if(!(args[0].equalsIgnoreCase("/p") && args[1].equalsIgnoreCase("h"))) {
                return null;
            }

            if(isInteger(args[2])) {
                int currentPlot = Integer.parseInt(args[2]);
                if(direction == DIRECTION.PREVIOUS) {
                    return args[0] + " " + args[1] + " " + (currentPlot - 1);
                } else {
                    return args[0] + " " + args[1] + " " + (currentPlot + 1);
                }
            } else {
                if(direction == DIRECTION.PREVIOUS) {
                    return null;
                } else {
                    return command + " 2";
                }
            }
        } else if(args.length == 4) {
            if(!(args[0].equalsIgnoreCase("/p") && args[1].equalsIgnoreCase("h"))) {
                return null;
            }

            if(!isInteger(args[3])) {
                return null;
            } else {
                int nextPlot = Integer.parseInt(args[3]);
                if(direction == DIRECTION.NEXT) {
                    nextPlot++;
                } else {
                    nextPlot--;
                }

                if(nextPlot < 1) {
                    return null;
                }

                return args[0] + " " + args[1] + " " + args[2] + " " +  nextPlot;
            }
        }
        return null;
    }

    private boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}