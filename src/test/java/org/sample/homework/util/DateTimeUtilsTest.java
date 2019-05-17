package org.sample.homework.util;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sample.homework.util.DateTimeUtils.parseDateTime;
import static org.sample.homework.util.DateTimeUtils.toPrettyDuration;

class DateTimeUtilsTest {

    @Test
    void toPrettyDurationTest_nominal() {
        assertEquals("0 seconds ago", toPrettyDuration(Duration.ofSeconds(0)));
        assertEquals("11 minutes ago", toPrettyDuration(Duration.of(700, ChronoUnit.SECONDS)));
        assertEquals("4 days ago", toPrettyDuration(Duration.ofDays(4)));
    }

    @Test
    void toPrettyDurationTest_shouldThrowWhenNull() {
        assertThrows(NullPointerException.class, () -> toPrettyDuration(null));
    }

    @Test
    void parseZonedDateTimeTest_datesAreParsedCorrectly() {
        // GIVEN
        String dateToParse = "09/May/2018:16:00:39 +0000";
        ZonedDateTime expected = ZonedDateTime.of(2018, 5, 9, 16, 0, 39, 0, ZoneOffset.UTC);

        // WHEN
        ZonedDateTime result = parseDateTime(dateToParse);

        // THEN
        assertEquals(expected, result);
    }

    @Test
    void parseZonedDateTimeTest_nullStr() {
        assertThrows(NullPointerException.class, () -> parseDateTime(null));
    }

    @Test
    void parseZonedDateTimeTest_badFormat() {
        assertThrows(DateTimeParseException.class, () -> parseDateTime("2018-05-09T16:00:39 +0000"));
    }
}