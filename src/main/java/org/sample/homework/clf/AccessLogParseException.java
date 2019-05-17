package org.sample.homework.clf;

/**
 * An exception for everything that goes wrong when parsing a Common Log Format log line.
 *
 * @see AccessLogParser#parse(String)
 */
public class AccessLogParseException extends Exception {

    AccessLogParseException(String message) {
        super(message);
    }

    AccessLogParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
