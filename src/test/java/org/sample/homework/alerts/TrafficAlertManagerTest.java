package org.sample.homework.alerts;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sample.homework.stats.TrafficStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contains the alerting logic tests.
 */
class TrafficAlertManagerTest {

    /**
     * A traffic alert listener implementation.
     */
    private static class TrafficAlertListener {

        @Subscribe
        void receiveTrafficAlert(TrafficAlert alert) {
            trafficAlerts.add(alert);
        }
    }

    /**
     * The event bus used to get traffic alerts.
     */
    private final static EventBus eventBus = new EventBus();

    /**
     * Holds the list of all triggered traffic alerts.
     */
    private final static List<TrafficAlert> trafficAlerts = new ArrayList<>();

    @BeforeAll
    static void before() {
        eventBus.register(new TrafficAlertManagerTest.TrafficAlertListener());
    }

    @Test
    void alertingLogicTest() {
        // Create a traffic alert manager.
        int trafficAlertThreshold = 10;
        int monitorDurationSeconds = 120;
        int maxTrafficStats = 10;
        TrafficAlertManager store = new TrafficAlertManager(maxTrafficStats, trafficAlertThreshold, monitorDurationSeconds, eventBus);

        // We don't expect alerts at this point.
        assertTrue(trafficAlerts.isEmpty());

        // Feed the traffic alert manager some traffic stats.
        Stream.of(70, 80, 90, 100, 110, 120, 130, 140, 150, 160)
                .map(TrafficAlertManagerTest::createTrafficStatistics)
                .forEach(store::handleTrafficStatistics);

        // Total hits is equal to 1150, which is not enough to trigger a traffic alert (1150 / 120 < 10)
        assertEquals(1150, store.getTotalHitsDuringMonitorDuration());
        assertTrue(trafficAlerts.isEmpty());

        // Add a new traffic stats entry.
        store.handleTrafficStatistics(createTrafficStatistics(170));

        // Total hits is high enough to trigger a traffic alert (1250 exactly which is 1150 - 70 + 170).
        assertEquals(1250, store.getTotalHitsDuringMonitorDuration());
        assertEquals(1, trafficAlerts.size());
        TrafficAlert alert = trafficAlerts.get(0);
        assertEquals(TrafficAlert.AlertType.HIGH_TRAFFIC, alert.getType());
        assertEquals((float) 1250 / 120, alert.getHitsPerSecond());

        // If we don't drop bellow 10 hits/s on average, we don't expect another high traffic alert.
        store.handleTrafficStatistics(createTrafficStatistics(100));
        assertEquals(1270, store.getTotalHitsDuringMonitorDuration());
        assertEquals(1, trafficAlerts.size());

        // If we drop bellow 10 hits/s on average, we expect a traffic recovery alert.
        store.handleTrafficStatistics(createTrafficStatistics(0));
        assertEquals(1180, store.getTotalHitsDuringMonitorDuration());
        assertEquals(2, trafficAlerts.size());
        alert = trafficAlerts.get(1);
        assertEquals(TrafficAlert.AlertType.RECOVERED, alert.getType());
        assertEquals((float) 1180 / 120, alert.getHitsPerSecond());
    }

    private static TrafficStatistics createTrafficStatistics(int hits) {
        TrafficStatistics statistics = new TrafficStatistics();
        statistics.setTotalRequestCount(hits);
        return statistics;
    }
}