package org.sample.homework.ui;

import com.google.common.eventbus.Subscribe;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.sample.homework.alerts.TrafficAlert;
import org.sample.homework.stats.TrafficStatistics;

import java.io.IOException;

/**
 * Responsible from creating the ui component and printing traffic summaries and alerts to the screen.
 */
public class ConsoleGui {

    /**
     * The main statistics window.
     */
    private final StatsWindow window = new StatsWindow();

    /**
     * Function to receive traffic statistics events from the event bus.
     *
     * @param statistics the traffic statistics event
     */
    @Subscribe
    public void handleTrafficStatistics(TrafficStatistics statistics) {
        window.handleTrafficStatistics(statistics);
    }

    /**
     * Function to receive traffic alerts events from the event bus.
     *
     * @param alert the traffic alert event
     */
    @Subscribe
    public void handleTrafficAlert(TrafficAlert alert) {
        window.handleTrafficAlert(alert);
    }

    /**
     * Starts the gui and draw content to the console.
     *
     * @param exitCallback a callback used to notify that the screen is closed
     * @throws IOException if something goes wrong while initializing the gui
     */
    public void start(Runnable exitCallback) throws IOException {
        // Setup terminal and screen layers
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        try (Screen screen = new TerminalScreen(terminal)) {
            screen.setCursorPosition(null);
            screen.startScreen();
            terminal.addResizeListener((terminal1, terminalSize) -> window.onTerminalResize(terminalSize));
            window.onTerminalResize(screen.getTerminalSize());
            // Create and start gui.
            MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(),
                    null, new EmptySpace(TextColor.ANSI.BLACK));
            // This call will run the event/update loop and won't return until "window" is closed.
            gui.addWindowAndWait(window);
            exitCallback.run();
        }
    }
}
