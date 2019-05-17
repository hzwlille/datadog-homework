package org.sample.homework;

import lombok.Data;

/**
 * Contains the application's default options. These options are replaced by user arguments if any.
 */
@Data
class ApplicationOptions {

    /**
     * The access log file location.
     */
    private String fileLocation = "/tmp/access.log";

    /**
     * The interval in seconds during which the stats are computed.
     */
    private int reportInterval = 10;

    /**
     * Threshold (number of hits per second) to be used to generate stats alerts.
     */
    private int alertThreshold = 100;

    /**
     * The duration in seconds during which stats alerts are computed.
     */
    private int monitorDuration = 120;

}
