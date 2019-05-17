package org.sample.homework;

import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.sample.homework.alerts.TrafficAlertManager;
import org.sample.homework.clf.AccessLogRecordProducer;
import org.sample.homework.stats.TrafficStatisticsManager;
import org.sample.homework.ui.ConsoleGui;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Starter class.
 */
@Slf4j
public class Application {

    /**
     * Application entry point.
     *
     * @param args application command line arguments
     */
    public static void main(String[] args) {
        run(args);
    }

    /**
     * Runs the application.
     *
     * @param args an array of {@link String} arguments to be parsed
     */
    private static void run(String[] args) {
        ApplicationOptions options = null;
        try {
            CommandLine commandLine = parseArguments(args);
            options = validateArguments(commandLine);
            log.info("Initializing the application with the given options {}", options);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            printApplicationHelp();
            System.exit(1);
        }

        // Implements a central event bus used for high level communication between application components.
        EventBus eventBus = new EventBus();

        TrafficStatisticsManager stats = new TrafficStatisticsManager(options.getReportInterval(), eventBus);

        // Initialise the traffic alerting manager.
        TrafficAlertManager monitoringStore =
                new TrafficAlertManager(options.getMonitorDuration() / options.getReportInterval(),
                        options.getAlertThreshold(), options.getMonitorDuration(), eventBus);

        // We can subscribe to an event by registering our components on the EventBus.
        eventBus.register(stats);
        eventBus.register(monitoringStore);

        // The UI execution is performed asynchronously using a separate thread.
        new Thread(() -> {
            ConsoleGui gui = new ConsoleGui();
            eventBus.register(gui);
            try {
                gui.start(() -> System.exit(0));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                System.exit(1);
            }
        }, "ui-thread").start();

        try {
            // Create and start the access log record producer (this is blocking).
            new AccessLogRecordProducer(options.getFileLocation(), eventBus);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }

    }

    /**
     * Parses application arguments.
     *
     * @param args the application arguments to parse
     * @return {@link CommandLine} which represents the list of application arguments
     * @throws ParseException if there are any problems encountered while parsing the command line tokens
     */
    private static CommandLine parseArguments(String[] args) throws ParseException {
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    /**
     * Validates user passed application arguments.
     *
     * @param commandLine the {@link CommandLine} which represents the list of application arguments, not <tt>null</tt>
     * @return a valid {@link ApplicationOptions}, not <tt>null</tt>
     */
    private static ApplicationOptions validateArguments(CommandLine commandLine) {
        ApplicationOptions options = new ApplicationOptions();
        // Validate access log path.
        String logFile = commandLine.getOptionValue("log-file");
        if (logFile != null) {
            options.setFileLocation(logFile);
        }

        // Do not allow this to be a folder since we want to watch files.
        if (!Paths.get(options.getFileLocation()).toFile().isFile()) {
            throw new IllegalArgumentException(options.getFileLocation() + " doesn't exist or is not a regular file!");
        }

        // Validate report interval.
        String reportInterval = commandLine.getOptionValue("report-interval");
        if (reportInterval != null) {
            try {
                int result = Integer.parseInt(reportInterval);
                if (result < 1) {
                    throw new IllegalArgumentException("Invalid 'report-interval' argument value: " + reportInterval);
                }
                options.setReportInterval(result);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid 'report-interval' argument value: " + reportInterval);
            }
        }

        // Validate alert threshold.
        String alertThreshold = commandLine.getOptionValue("alert-threshold");
        if (alertThreshold != null) {
            try {
                int result = Integer.parseInt(alertThreshold);
                if (result < 1) {
                    throw new IllegalArgumentException("Invalid 'alert-threshold' argument value: " + alertThreshold);
                }
                options.setAlertThreshold(result);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid 'alert-threshold' argument value: " + alertThreshold);
            }
        }

        // Validate alert duration.
        String alertDuration = commandLine.getOptionValue("monitor-duration");
        if (alertDuration != null) {
            try {
                int result = Integer.parseInt(alertDuration);
                if (result < options.getReportInterval()) {
                    throw new IllegalArgumentException(String.format("%s should be bigger than %s! Got %s and %d.",
                            "monitor-duration", "report-interval", alertDuration, options.getReportInterval()));
                }
                options.setMonitorDuration(result);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid 'monitor-duration' argument value: " + alertDuration);
            }
        }

        return options;
    }

    /**
     * Generates application command line options.
     *
     * @return application {@link Options}
     */
    private static Options getOptions() {
        ApplicationOptions defaults = new ApplicationOptions();
        Options options = new Options();
        options.addOption("f", "log-file", true,
                "access log file location, default " + defaults.getFileLocation());
        options.addOption("r", "report-interval", true,
                "interval for showing stats reports in seconds, default " + defaults.getReportInterval());
        options.addOption("t", "alert-threshold", true,
                "alert threshold in hits/sec, default " + defaults.getAlertThreshold());
        options.addOption("d", "monitor-duration", true,
                "alert duration in seconds, default " + defaults.getMonitorDuration());
        return options;
    }

    /**
     * Prints application help.
     */
    private static void printApplicationHelp() {
        Options options = getOptions();
        HelpFormatter formatter = new HelpFormatter();
        // Reserve enough space to print descriptions in one line.
        formatter.setWidth(100);
        formatter.printHelp("./stats.sh", options, true);
    }
}