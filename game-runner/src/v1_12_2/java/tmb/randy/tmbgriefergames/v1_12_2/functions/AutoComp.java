package tmb.randy.tmbgriefergames.v1_12_2.functions;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.CompressorState;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.core.helper.I19n;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;
import tmb.randy.tmbgriefergames.v1_12_2.click.Click;
import tmb.randy.tmbgriefergames.v1_12_2.click.ClickManager;

public class AutoComp extends ActiveFunction {

    private int counter = 0;
    private CompressorState compState = CompressorState.NONE;
    private final LinkedList<Click> toSend = new LinkedList<>();
    private final List<CompressorState> list1 = Arrays.asList(CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS3, CompressorState.COMPRESS4, CompressorState.COMPRESS5);
    private final List<CompressorState> list2 = Arrays.asList(CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1 , CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS3, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1 , CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS3, CompressorState.COMPRESS4, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1 , CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS3, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1 , CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS3, CompressorState.COMPRESS4, CompressorState.COMPRESS5, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1 , CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS3, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1 , CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS3, CompressorState.COMPRESS4, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1 , CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS3, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1, CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS1 , CompressorState.COMPRESS1, CompressorState.COMPRESS2, CompressorState.COMPRESS3, CompressorState.COMPRESS4, CompressorState.COMPRESS5, CompressorState.COMPRESS6);

    private final List<List<CompressorState>> lists = Arrays.asList(list1, list2);

    private int currentList = 0;
    private int currentEntry = 0;

    public AutoComp() {
        super(Functions.COMP);
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if(event.state() == State.PRESS && isEnabled()) {
            if(event.key() == Key.ESCAPE) {
                stop();
                return;
            }

            if (Helper.getPlayer().openContainer != null) {

                if(Helper.getPlayer().openContainer instanceof ContainerChest chest) {
                    IInventory inv = chest.getLowerChestInventory();
                    if(inv.getName().equals("§6Item-Komprimierung")) {
                        if (event.key() == Key.ARROW_UP) {
                            changeList(true);
                        } else if (event.key() == Key.ARROW_DOWN) {
                            changeList(false);
                        }
                    }
                }
            } else {
                if (Addon.getSharedInstance().allKeysPressed(Addon.getSharedInstance().configuration().getAutoCrafterConfig().getAutoCompHotkey().get())) {
                    start();
                }
            }
        }
    }

    @Override
    public void cbChangedEvent(CbChangedEvent event) {
        stop();
    }

    @Override
    public void tickEvent(GameTickEvent event) {

        if(compState == CompressorState.NONE) 
            return;
        
        counter++;

        if(counter >= 8) 
            counter = 0;
         else 
            return;

        Container cont = Helper.getPlayer().openContainer;
        if(cont instanceof ContainerChest chest) {

            IInventory inv = chest.getLowerChestInventory();
            if(inv.getName().equalsIgnoreCase("§6Custom-Kategorien") && compState == CompressorState.WAITING_FOR_MENU1) {
                compState = CompressorState.WAITING_FOR_MENU2;
                click(11);

            } else if(inv.getName().equalsIgnoreCase("§6Item-Komprimierung-Bauanleitung")) {
                compState = CompressorState.COMPRESS1;
                click(81);
            } else if(inv.getName().equalsIgnoreCase("§6Item-Komprimierung")) {
                ItemStack pageIndicator = chest.getSlot(49).getStack();
                if(Item.getIdFromItem(pageIndicator.getItem()) == 397) {
                    String page = pageIndicator.getDisplayName();

                    int direction = getClickDirection(page);

                    if(direction == -1) {
                        click(48);
                    } else if(direction == 0) {
                        click(52);
                        if(currentEntry >= lists.get(currentList).size() - 1) {
                            currentEntry = 0;
                        } else {
                            currentEntry++;
                        }
                    } else if(direction == 1) {
                        click(50);
                    }
                }
            }
        }
    }

    @Override
    public boolean start() {
        if(super.start()) {
            compState = CompressorState.WAITING_FOR_MENU1;
            Addon.sendCommand("/rezepte");
            return true;
        }

        return false;
    }

    @Override
    public boolean stop() {
        if(super.stop()) {
            compState = CompressorState.NONE;
            currentList = 0;
            currentEntry = 0;
            return true;
        }
        return false;
    }

    private int getClickDirection(String current) {
        switch (current) {
            case "§6Komprimierungsstufe 1":
                switch (lists.get(currentList).get(currentEntry)) {
                    case COMPRESS1:
                        return 0;
                    case COMPRESS2, COMPRESS3, COMPRESS4, COMPRESS5, COMPRESS6:
                        return 1;
                    default:
                        break;
                }
                break;
            case "§6Komprimierungsstufe 2":
                switch (lists.get(currentList).get(currentEntry)) {
                    case COMPRESS1:
                        return -1;
                    case COMPRESS2:
                        return 0;
                    case COMPRESS3, COMPRESS4, COMPRESS5, COMPRESS6:
                        return 1;
                    default:
                        break;
                }
                break;
            case "§6Komprimierungsstufe 3":
                switch (lists.get(currentList).get(currentEntry)) {
                    case COMPRESS1, COMPRESS2:
                        return -1;
                    case COMPRESS3:
                        return 0;
                    case COMPRESS4, COMPRESS5, COMPRESS6:
                        return 1;
                    default:
                        break;
                }
                break;
            case "§6Komprimierungsstufe 4":
                switch (lists.get(currentList).get(currentEntry)) {
                    case COMPRESS1, COMPRESS2, COMPRESS3:
                        return -1;
                    case COMPRESS4:
                        return 0;
                    case COMPRESS5, COMPRESS6:
                        return 1;
                    default:
                        break;
                }
                break;
            case "§6Komprimierungsstufe 5":
                switch (lists.get(currentList).get(currentEntry)) {
                    case COMPRESS1, COMPRESS2, COMPRESS3, COMPRESS4:
                        return -1;
                    case COMPRESS5:
                        return 0;
                    case COMPRESS6:
                        return 1;
                    default:
                        break;
                }
                break;
            case "§6Komprimierungsstufe 6":
                switch (lists.get(currentList).get(currentEntry)) {
                    case COMPRESS1, COMPRESS2, COMPRESS3, COMPRESS4, COMPRESS5:
                        return -1;
                    case COMPRESS6:
                        return 0;
                    default:
                        break;
                }
                break;
            case "§6Komprimierungsstufe 7":
                switch (lists.get(currentList).get(currentEntry)) {
                    case COMPRESS1, COMPRESS2, COMPRESS3, COMPRESS4, COMPRESS5, COMPRESS6:
                        return -1;
                    default:
                        break;
                }
                break;
        }

        return 0;
    }

    public void changeList(boolean increase) {

        if((currentList >= lists.size()-1 && increase) || (currentList <= 0 && !increase)) {
            return;
        }

        counter = 0;
        currentEntry = 0;

        if(increase) {
            currentList++;
        } else {
            currentList--;
        }

        Addon.getSharedInstance().displayNotification(
            I19n.translate("autoComp.list") + (currentList + 1));
    }

    private void click(int slot) {
        this.toSend.addLast(new Click(Helper.getPlayer().openContainer.windowId, slot, 0, ClickType.QUICK_MOVE));
        ClickManager.getSharedInstance().queueClicks(QueueType.MEDIUM, this.toSend);
        this.toSend.clear();
    }
}