package org.sample.homework.alerts;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.text.MessageFormat;
import java.time.ZonedDateTime;

/**
 * An immutable class representing a traffic alert.
 */
@Getter
public class TrafficAlert {

    /**
     * An enum defining all alert types.
     */
    @Getter
    @RequiredArgsConstructor
    public enum AlertType {
        HIGH_TRAFFIC("High traffic generated an alert - {0} hits/s\nTriggered at {1}"),
        RECOVERED("Recovered at {1} - {0} hits/s");

        /**
         * Message format used to construct the alert.
         */
        private final String message;
    }

    /**
     * The stats alert type.
     */
    private final AlertType type;

    /**
     * The exact number of hits per second which triggered this alert.
     */
    private final float hitsPerSecond;

    /**
     * The date at which this alert has been created.
     */
    private final ZonedDateTime time;

    /**
     * Holds the formatted traffic alert message.
     */
    private final String message;

    /**
     * Class constructor.
     *
     * @param type          the type of the traffic alert, not <tt>null</tt>
     * @param hitsPerSecond hits per seconds which triggered this alert
     */
    TrafficAlert(@NonNull AlertType type, float hitsPerSecond) {
        this.type = type;
        this.hitsPerSecond = hitsPerSecond;
        this.time = ZonedDateTime.now();
        this.message = MessageFormat.format(type.getMessage(), hitsPerSecond, time.toLocalTime().withNano(0));
    }

}
