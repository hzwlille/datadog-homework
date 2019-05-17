package org.sample.homework.clf;

/**
 * An interface to be implemented by Common Log Format parsers.
 */
public interface AccessLogParser {

    /**
     * Parses a Common Log Format line into a {@link AccessLogRecord} object.
     *
     * @param line the log line to parse
     * @return a valid non-<tt>null</tt> {@link AccessLogRecord} based on the log line
     * @throws AccessLogParseException if the line to parse is not valid
     */
    AccessLogRecord parse(String line) throws AccessLogParseException;
}
