package tmb.randy.tmbgriefergames.core.dev;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.commands.DescribedCommand;
import tmb.randy.tmbgriefergames.core.flow.editor.FlowEditorActivity;

/**
 * Remote control for automated tests, only active in the addon dev environment (never in the released addon).
 * Lines written to {@code <game dir>/tmb-dev/commands.txt} are executed one after another:
 * <ul>
 *   <li>{@code wait <ticks>} pauses</li>
 *   <li>{@code screenshot <name>} saves a screenshot to {@code screenshots/<name>.png}</li>
 *   <li>{@code close} closes the open screen</li>
 *   <li>everything else is sent like typed into the chat (addon commands are executed directly)</li>
 * </ul>
 * Every executed line is appended to {@code tmb-dev/log.txt}.
 */
public class DevBridge {
    private final Path directory = Laby.labyAPI().labyModLoader().getGameDirectory().resolve("tmb-dev");
    private final Path commandFile = directory.resolve("commands.txt");
    private final Path logFile = directory.resolve("log.txt");
    private final Deque<String> queue = new ArrayDeque<>();
    private int waitTicks;

    public static boolean isEnabled() {
        return Boolean.getBoolean("net.labymod.addon-dev-environment");
    }

    @Subscribe
    public void onTick(GameTickEvent event) {
        if (event.phase() != Phase.POST)
            return;

        readCommandFile();
        if (waitTicks > 0) {
            waitTicks--;
            return;
        }

        while (!queue.isEmpty() && waitTicks == 0)
            execute(queue.poll());
    }

    private void readCommandFile() {
        if (!Files.isRegularFile(commandFile))
            return;

        try {
            for (String line : Files.readAllLines(commandFile, StandardCharsets.UTF_8)) {
                if (!line.isBlank())
                    queue.add(line.trim());
            }
            Files.delete(commandFile);
        } catch (IOException e) {
            Addon.log().error("DevBridge could not read the command file", e);
        }
    }

    private void execute(String line) {
        log(line);
        String[] parts = line.split(" ", 2);
        String argument = parts.length > 1 ? parts[1].trim() : "";

        switch (parts[0].toLowerCase()) {
            case "wait" -> waitTicks = Math.max(0, Integer.parseInt(argument));
            case "screenshot" -> Laby.labyAPI().minecraft().executeOnRenderThread(() -> Addon.getConnection().takeScreenshot(argument));
            case "close" -> Addon.getConnection().closeScreen();
            case "click", "rightclick" -> click(argument, parts[0].equalsIgnoreCase("rightclick") ? (MouseButton) MouseButton.RIGHT : (MouseButton) MouseButton.LEFT);
            case "dump" -> {
                if (FlowEditorActivity.getOpenEditor() != null)
                    FlowEditorActivity.getOpenEditor().dumpWidgets(text -> log(text));
            }
            default -> chat(line);
        }
    }

    /** Clicks into the open flow editor at GUI coordinates "x y". */
    private void click(String argument, MouseButton button) {
        FlowEditorActivity editor = FlowEditorActivity.getOpenEditor();
        String[] coordinates = argument.split(" ");
        if (editor == null || coordinates.length < 2)
            return;

        MutableMouse mouse = new MutableMouse(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
        log("  clicked: " + editor.mouseClicked(mouse, button) + ", released: " + editor.mouseReleased(mouse, button));
    }

    /** Addon commands are executed directly, everything else goes through the chat like typed by the player. */
    private void chat(String line) {
        if (line.startsWith("/")) {
            String[] words = line.substring(1).split(" ");
            for (DescribedCommand command : Addon.getCommands()) {
                if (command.matches(words[0])) {
                    command.execute(words[0], Arrays.copyOfRange(words, 1, words.length));
                    return;
                }
            }
        }
        Laby.labyAPI().minecraft().chatExecutor().chat(line, false);
    }

    private void log(String text) {
        try {
            Files.createDirectories(directory);
            Files.writeString(logFile, LocalTime.now().withNano(0) + " " + text + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            Addon.log().error("DevBridge could not write the log", e);
        }
    }
}
