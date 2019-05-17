package org.sample.homework.stats;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sample.homework.clf.AccessLogRecord;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Map.Entry.comparingByValue;
import static org.sample.homework.clf.CommonLogFormatUtils.extractSection;

/**
 * This class is responsible for generating stats summary based on the consumed access log records.
 */
@Slf4j
public class TrafficStatisticsManager {

    /**
     * Thread-safe lock-free deque to keep track of latest access log records.
     * <p>
     * {@link ConcurrentLinkedDeque}s are very fast for insertions and removals at both ends. The advantage of using
     * this data structure is that the timer thread can process the appropriate access log records while this deque is
     * always available for adds.
     */
    private final Deque<AccessLogRecord> accessLogRecords = new ConcurrentLinkedDeque<>();

    /**
     * A small delay in order to not loose access log records in case of I/O latencies while reading the log file.
     * Should be slightly bigger than the polling value used by the file watcher.
     */
    private static final TemporalAmount DELAY = Duration.ofMillis(600);

    /**
     * During very timer tick we only process records older than this timestamp.
     */
    private Instant maxTimestamp = Instant.now().minus(DELAY);

    /**
     * The event bus which is used to publish traffic statistics.
     */
    private final EventBus eventBus;

    /**
     * Class constructor.
     *
     * @param refreshPeriodSeconds period at which to compute stats summary in seconds
     * @param eventBus             the event bus to use to publish traffic summaries
     */
    public TrafficStatisticsManager(int refreshPeriodSeconds, @NonNull EventBus eventBus) {
        this.eventBus = eventBus;
        // Define the timer task scheduled at a fixed rate.
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                updateTrafficStatistics();
                maxTimestamp = maxTimestamp.plusSeconds(refreshPeriodSeconds);
            }
        };
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        // Note that if an execution is delayed for any reason (such as GC or other background activity),
        // two or more executions will occur in rapid succession to “catch up”.
        executor.scheduleAtFixedRate(repeatedTask, 0, refreshPeriodSeconds, TimeUnit.SECONDS);
    }

    /**
     * Creates stats statistics for the current period.
     */
    private void updateTrafficStatistics() {
        TrafficStatistics statistics = new TrafficStatistics();
        Set<String> uniqueHosts = new HashSet<>();
        // Used to compute the sections of the web site with the most hits.
        Map<String, Integer> hitsBySection = new HashMap<>();
        // Remove and process access log records which fit in this window frame.
        AccessLogRecord record = accessLogRecords.pollLast();
        while (record != null && record.getDateTime().toInstant().isBefore(maxTimestamp)) {
            // Increment total request count.
            statistics.incrementTotalRequestCount();
            // Increment valid request count based on the status code.
            if (record.getStatus() >= 200 && record.getStatus() < 300) {
                statistics.incrementValidRequestCount();
            }
            // Increment bytes transferred.
            statistics.addBytesTransferred(record.getBytes());
            // Increments hits by section and hits by method.
            // Here, instead of fetching the value from the map, increment it and put it back into the map,
            // we take advantage of compute which takes a lambda and computes the value in an atomic way. This is O(1).
            hitsBySection.compute(extractSection(record.getEndpoint()), (k, v) -> v == null ? 1 : ++v);
            statistics.getHitsByMethod().compute(record.getMethod(), (k, v) -> v == null ? 1 : ++v);
            // If we have a host add it to our unique set.
            if (record.getHost() != null) {
                uniqueHosts.add(record.getHost());
            }
            record = accessLogRecords.pollLast();
        }
        // Keep only top 5 sections.
        hitsBySection.entrySet().stream()
                .sorted(Collections.reverseOrder(comparingByValue()))
                .limit(5)
                .forEach(e -> statistics.getHitsBySection().add(e));
        statistics.setUniqueHosts(uniqueHosts.size());

        // Publish the computed traffic stats to the event bus.
        eventBus.post(statistics);
    }

    /**
     * Handler to get {@link AccessLogRecord}s from the {@link EventBus}.
     * We annotate the handler method with {@link Subscribe}.
     *
     * @param record the access log record consumed from the event bus
     */
    @Subscribe
    public void handleAccessLogRecord(AccessLogRecord record) {
        // Add the record at the front of the buffer.
        accessLogRecords.addFirst(record);
    }

}
