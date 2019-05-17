package org.sample.homework.clf;

import lombok.NonNull;
import org.sample.homework.util.DateTimeUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.sample.homework.clf.CommonLogFormatUtils.*;

/**
 * A regular expression based {@link AccessLogParser} implementation.
 */
public class RegexAccessLogParser implements AccessLogParser {

    /**
     * The regular expression that matches a line in the log files following the Common Log Format.
     * <p>
     * Each line in a file stored in the Common Log Format has the following syntax:
     * host ident authuser date request status bytes
     * Example: 127.0.0.1 - james [09/May/2018:16:00:39 +0000] "GET /report HTTP/1.2" 200 123
     * <p>
     * Note that for performance reasons the regex is compiled into a pattern.
     */
    private static final Pattern ACCESS_LOG_PATTERN = Pattern
            .compile("^(\\S+) (\\S+) (\\S+) \\[([^]]+)] \"([A-Z]+) ([^ \"]+) ?([^\"]+)?\" ([0-9]{3}) ([0-9]+|-)$");

    /**
     * Parses a Common Log Format log line into a {@link AccessLogRecord} object.
     *
     * @param line the log line to parse, not <tt>null</tt>
     * @return a valid record whose attribute values come from the fields found in the log line, non <tt>null</tt>
     * @throws AccessLogParseException if the line to parse is not valid
     * @see RegexAccessLogParser#ACCESS_LOG_PATTERN
     */
    @Override
    public AccessLogRecord parse(@NonNull String line) throws AccessLogParseException {
        // Create a matcher for the input string.
        Matcher matcher = ACCESS_LOG_PATTERN.matcher(line);
        if (matcher.find()) {
            try {
                // Parse the log line and initialize an AccessLogRecord object using the matched groups.
                return AccessLogRecord.builder()
                        .host(nullifyIfDash(matcher.group(1)))
                        .userIdentifier(nullifyIfDash(matcher.group(2)))
                        .user(nullifyIfDash(matcher.group(3)))
                        .dateTime(DateTimeUtils.parseDateTime(matcher.group(4)))
                        .method(matcher.group(5))
                        .endpoint(matcher.group(6))
                        .protocol(matcher.group(7))
                        .status(Integer.parseInt(matcher.group(8)))
                        .bytes(parseContentSize(matcher.group(9)))
                        .build();
            } catch (DateTimeParseException ex) {
                throw new AccessLogParseException("Invalid strftime format!", ex);
            }
        }

        // No match means the line format doesn't respect the Common Log Format.
        throw new AccessLogParseException("Invalid Common Log Format log line: " + line + '!');
    }

}
