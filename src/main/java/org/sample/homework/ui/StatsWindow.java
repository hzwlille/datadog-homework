package org.sample.homework.ui;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.io.FileUtils;
import org.sample.homework.alerts.TrafficAlert;
import org.sample.homework.stats.TrafficStatistics;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static java.util.Map.Entry.comparingByValue;
import static org.sample.homework.util.DateTimeUtils.toPrettyDuration;

/**
 * Responsible for creating the statistics window.
 */
class StatsWindow extends BasicWindow {

    /**
     * UI main panels.
     */
    private final Panel trafficStatsPanel = new Panel();
    private final Panel trafficAlertsPanel = new Panel();

    /**
     * A fixed-size circular buffer holding the latest 20 generated traffic alerts.
     */
    private final CircularFifoQueue<TrafficAlert> trafficAlerts = new CircularFifoQueue<>(20);

    /**
     * Used to print duration since the monitoring is running.
     */
    private final Instant startedAt = Instant.now();

    /**
     * Default class constructor.
     */
    StatsWindow() {
        Panel rootPanel = new Panel();
        Panel mainPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        trafficAlertsPanel.setLayoutManager(new LinearLayout());
        mainPanel.addComponent(trafficStatsPanel.withBorder(Borders.singleLine("Traffic Statistics")));
        mainPanel.addComponent(trafficAlertsPanel.withBorder(Borders.singleLine("Traffic Alerts")));
        rootPanel.addComponent(mainPanel);
        Panel statusPanel = new Panel();
        statusPanel.addComponent(new Label("Press '^C' to exit and return to terminal window!"));
        rootPanel.addComponent(statusPanel);
        setComponent(rootPanel);
        setHints(Arrays.asList(Hint.FULL_SCREEN, Hint.NO_DECORATIONS));
    }

    /**
     * Callback used to rearrange the layout on terminal resize.
     *
     * @param terminalSize the new terminal size to take into account
     */
    void onTerminalResize(TerminalSize terminalSize) {
        TerminalSize half = new TerminalSize(terminalSize.getColumns() / 2, terminalSize.getRows() - 3);
        trafficStatsPanel.setPreferredSize(half);
        trafficAlertsPanel.setPreferredSize(half);
    }

    /**
     * Prints traffic statistics.
     *
     * @param statistics the statistics summary to print
     */
    void handleTrafficStatistics(TrafficStatistics statistics) {
        trafficStatsPanel.removeAllComponents();
        trafficStatsPanel.addComponent(new Label("¤ Monitoring started " +
                toPrettyDuration(Duration.between(startedAt, Instant.now()))));
        trafficStatsPanel.addComponent(new Label("\nSummary").addStyle(SGR.BOLD));
        trafficStatsPanel.addComponent(new Label("Total Requests: " + statistics.getTotalRequestCount()));
        trafficStatsPanel.addComponent(new Label("2xx Requests: " + statistics.getValidRequestCount()));
        trafficStatsPanel.addComponent(new Label("Total Bytes Transferred: " +
                FileUtils.byteCountToDisplaySize(statistics.getBytesTransferred())));
        trafficStatsPanel.addComponent(new Label("Unique Hosts: " + statistics.getUniqueHosts()));
        trafficStatsPanel.addComponent(new Label("\nHits By Section").addStyle(SGR.BOLD));
        statistics.getHitsBySection()
                .forEach(entry -> trafficStatsPanel.addComponent(new Label(entry.getKey() + " " + entry.getValue())));
        trafficStatsPanel.addComponent(new Label("\nHits By Method").addStyle(SGR.BOLD));
        statistics.getHitsByMethod().entrySet().stream().sorted(Collections.reverseOrder(comparingByValue()))
                .forEach(entry -> trafficStatsPanel.addComponent(new Label(entry.getKey() + " " + entry.getValue())));
    }

    /**
     * Prints traffic alerts.
     *
     * @param alert the traffic alert to print
     */
    void handleTrafficAlert(TrafficAlert alert) {
        trafficAlerts.add(alert);
        trafficAlertsPanel.removeAllComponents();
        for (TrafficAlert trafficAlert : trafficAlerts) {
            Label label = new Label(trafficAlert.getMessage()).addStyle(SGR.BOLD);
            if (trafficAlert.getType() == TrafficAlert.AlertType.HIGH_TRAFFIC) {
                label.setForegroundColor(TextColor.ANSI.RED);
            } else {
                label.setForegroundColor(TextColor.ANSI.GREEN);
                label.setText(label.getText() + "\n ");
            }
            trafficAlertsPanel.addComponent(label);
        }
    }

}
