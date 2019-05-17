package org.sample.homework.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Date and time utilities class.
 */
@UtilityClass
public class DateTimeUtils {

    /**
     * Date formatter used to parse dates in strftime format. This format is the standard date formatting for UNIX.
     *
     * @see DateTimeUtils#parseDateTime(String)
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

    /**
     * @see DateTimeUtils#toPrettyDuration(Duration)
     */
    private static final List<Long> times = Arrays.asList(
            TimeUnit.DAYS.toMillis(365),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1));

    /**
     * @see DateTimeUtils#toPrettyDuration(Duration)
     */
    private static final List<String> timesString = Arrays.asList("year", "month", "day", "hour", "minute", "second");

    /**
     * Converts a {@link Duration} to human-friendly string.
     *
     * @param duration the duration to format, not <tt>null</tt>
     * @return a string representing the duration, not <tt>null</tt>
     * @throws NullPointerException if the given duration is <tt>null</tt>
     */
    public static String toPrettyDuration(@NonNull Duration duration) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times.size(); i++) {
            Long current = times.get(i);
            long temp = duration.toMillis() / current;
            if (temp > 0) {
                builder.append(temp).append(' ').append(timesString.get(i)).append(temp != 1 ? 's' : "").append(" ago");
                break;
            }
        }
        String result = builder.toString();
        if (result.isEmpty()) {
            return "0 seconds ago";
        }
        return result;
    }

    /**
     * Parses a date in the strftime format.
     *
     * @param date the string to parse, not <tt>null</tt>
     * @return a {@link ZonedDateTime} parsed from the input string, not <tt>null</tt>
     * @throws NullPointerException if the date to parse is <tt>null</tt>
     */
    public static ZonedDateTime parseDateTime(@NonNull String date) {
        return ZonedDateTime.parse(date, DATE_TIME_FORMATTER);
    }

}
