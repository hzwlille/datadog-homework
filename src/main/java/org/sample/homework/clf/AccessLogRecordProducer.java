package org.sample.homework.clf;

import com.google.common.eventbus.EventBus;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sample.homework.reader.FileWatcher;
import org.sample.homework.reader.LineListener;
import org.sample.homework.reader.PollingFileWatcher;

import java.io.IOException;

/**
 * This class is responsible for creating and publishing access log records parsed from the log file.
 */
@Getter
@Slf4j
public class AccessLogRecordProducer implements LineListener {

    /**
     * The event bus which is used to publish access log records.
     *
     * @see AccessLogRecordProducer#handle(String)
     */
    private final EventBus eventBus;

    /**
     * Used to parse Common Log Format lines into a {@link AccessLogRecord}s.
     */
    private final AccessLogParser accessLogParser = new RegexAccessLogParser();


    /**
     * Class constructor.
     *
     * @param filename the file to follow for new lines, not <tt>null</tt>
     * @param eventBus the central event bus which is used to publish access log events, not <tt>null</tt>
     * @throws IOException id something goes wrong when creating the file watcher
     */
    public AccessLogRecordProducer(@NonNull String filename, @NonNull EventBus eventBus) throws IOException {
        this.eventBus = eventBus;
        // Creates a polling file watcher with a polling delay of 400 ms.
        try (FileWatcher fileWatcher = new PollingFileWatcher(filename, this, 400)) {
            fileWatcher.start();
        }
    }

    /**
     * Creates {@link AccessLogRecord} instances out of every log line and post it to the event bus.
     *
     * @param line the new line forwarded by the file fileWatcher
     */
    @Override
    public void handle(String line) {
        try {
            AccessLogRecord record = accessLogParser.parse(line);
            eventBus.post(record);
        } catch (AccessLogParseException e) {
            log.warn("Failed to parse log line. Ignoring it.", e);
        }
    }
}
