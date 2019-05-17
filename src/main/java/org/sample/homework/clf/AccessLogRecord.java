package org.sample.homework.clf;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

/**
 * Immutable class which represents a parsed line from a Common Log Format log file.
 *
 * @see AccessLogParser#parse(String)
 */
@Data
@Builder
public class AccessLogRecord {

    /**
     * The IP address of the client (remote host) which made the request to the server.
     */
    private final String host;

    /**
     * The RFC 1413 identity of the client.
     */
    private final String userIdentifier;

    /**
     * The username as which the user has authenticated himself.
     */
    private final String user;

    /**
     * The date, time, and time zone that the request was received.
     */
    private final ZonedDateTime dateTime;

    /**
     * The HTTP request method (for example, "POST", "GET", etc.).
     */
    private final String method;

    /**
     * The HTTP request endpoint (resource url).
     */
    private final String endpoint;

    /**
     * The HTTP request protocol (for example, "HTTP/1.2").
     */
    private final String protocol;

    /**
     * The HTTP status code returned to the client.
     */
    private final int status;

    /**
     * The content-length of the document transferred, measured in bytes.
     */
    private final int bytes;

}
