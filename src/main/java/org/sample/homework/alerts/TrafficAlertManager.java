package org.sample.homework.alerts;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.sample.homework.stats.TrafficStatistics;

/**
 * This class is responsible for holding traffic statistics and generating traffic alerts.
 * <p>
 * In order to be memory-efficient this class stores traffic stats in fixed-size circular buffers.
 */
@Getter
public
class TrafficAlertManager {

    /**
     * A fixed-size circular buffer holding the latest computed traffic stats.
     */
    private final CircularFifoQueue<TrafficStatistics> trafficStatistics;

    /**
     * Number of requests per second before triggering an alert (high stats or recovery).
     */
    private final int trafficAlertThreshold;

    /**
     * The duration in seconds during which stats alerts are computed.
     * Note that {@link TrafficStatistics} are only kept during this time in seconds
     */
    private final int monitorDurationSeconds;

    /**
     * Total hits during the monitor duration.
     * <p>
     * Note that this value is updated on-the-fly during each timer tick. This optimization saves us from iterating
     * through the whole list of {@link TrafficStatistics} to compute the value.
     */
    private int totalHitsDuringMonitorDuration = 0;

    /**
     * Used to generate the stats alerts.
     *
     * @see TrafficAlertManager#processAlerts()
     */
    private boolean highTraffic = false;

    /**
     * The event bus which is used to publish traffic alerts.
     */
    private final EventBus eventBus;

    /**
     * Class constructor.
     *
     * @param maxTrafficStats        maximum size of the buffer holding traffic stats
     * @param trafficAlertThreshold  number of requests per second before printing an alert
     * @param monitorDurationSeconds duration in seconds during which stats alerts are computed
     * @param eventBus               the event bus to use to publish traffic alerts
     */
    public TrafficAlertManager(int maxTrafficStats,
                               int trafficAlertThreshold,
                               int monitorDurationSeconds,
                               @NonNull EventBus eventBus) {
        this.trafficStatistics = new CircularFifoQueue<>(maxTrafficStats);
        this.trafficAlertThreshold = trafficAlertThreshold;
        this.monitorDurationSeconds = monitorDurationSeconds;
        this.eventBus = eventBus;
    }

    /**
     * Adds a new {@link TrafficStatistics} to the store.
     *
     * @param statistics the access log metrics to add, not <tt>null</tt>
     */
    private void addTrafficStatistics(TrafficStatistics statistics) {
        // Update totalHitsDuringMonitorDuration value.
        if (trafficStatistics.isAtFullCapacity()) {
            TrafficStatistics old = trafficStatistics.remove();
            totalHitsDuringMonitorDuration -= old.getTotalRequestCount();
        }
        totalHitsDuringMonitorDuration += statistics.getTotalRequestCount();

        // Add the statistics to the buffer.
        trafficStatistics.add(statistics);

        // See if we can generate an alert.
        processAlerts();
    }

    /**
     * Function responsible for generating stats alerts (high stats and recovery).
     */
    private void processAlerts() {
        float hitsPerSecond = (float) totalHitsDuringMonitorDuration / monitorDurationSeconds;
        if (highTraffic) {
            if (hitsPerSecond < trafficAlertThreshold) {
                highTraffic = false;
                eventBus.post(new TrafficAlert(TrafficAlert.AlertType.RECOVERED, hitsPerSecond));
            }
        } else if (hitsPerSecond > trafficAlertThreshold) {
            highTraffic = true;
            eventBus.post(new TrafficAlert(TrafficAlert.AlertType.HIGH_TRAFFIC, hitsPerSecond));
        }
    }

    @Subscribe
    public void handleTrafficStatistics(@NonNull TrafficStatistics statistics) {
        addTrafficStatistics(statistics);
    }

}
