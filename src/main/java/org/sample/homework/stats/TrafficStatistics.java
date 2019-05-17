package org.sample.homework.stats;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds stats statistics gathered during a window frame.
 */
@Data
public
class TrafficStatistics {

    /**
     * The total request count.
     */
    private int totalRequestCount = 0;

    /**
     * Total requests with 2xx response status.
     */
    private int validRequestCount = 0;

    /**
     * Total amount of bytes transferred.
     */
    private long bytesTransferred = 0;

    /**
     * An ordered list which contains sections of the web site with the most hits.
     */
    private final List<Map.Entry<String, Integer>> hitsBySection = new ArrayList<>(5);

    /**
     * A map used to compute number of hits by http method.
     */
    private final Map<String, Integer> hitsByMethod = new HashMap<>();

    /**
     * Unique remote host count.
     */
    private int uniqueHosts = 0;

    /**
     * Increments total request count by one.
     */
    void incrementTotalRequestCount() {
        ++totalRequestCount;
    }

    /**
     * Increments valid request count by one.
     */
    void incrementValidRequestCount() {
        ++validRequestCount;
    }

    /**
     * Increments total bytes transferred by the given amount.
     *
     * @param amount the amount of bytes to add
     */
    void addBytesTransferred(int amount) {
        bytesTransferred += amount;
    }

}
