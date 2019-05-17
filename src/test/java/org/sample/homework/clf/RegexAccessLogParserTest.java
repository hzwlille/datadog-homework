package org.sample.homework.clf;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegexAccessLogParserTest {

    private final AccessLogParser parser = new RegexAccessLogParser();

    @Test
    void parseAccessLogLineTest_nominal() throws AccessLogParseException {
        // GIVEN
        String line = "127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] \"GET /files/apache_pb.gif HTTP/1.0\" 200 2326";
        AccessLogRecord expected = AccessLogRecord.builder()
                .host("127.0.0.1")
                .userIdentifier(null)
                .user("frank")
                .dateTime(ZonedDateTime.of(2000, 10, 10, 13, 55, 36, 0, ZoneOffset.ofHours(-7)))
                .method("GET")
                .endpoint("/files/apache_pb.gif")
                .protocol("HTTP/1.0")
                .status(200)
                .bytes(2326)
                .build();

        // WHEN
        AccessLogRecord result = parser.parse(line);

        // THEN
        assertEquals(expected, result);
    }

    @Test
    void parseAccessLogLineTest_shouldThrowWhenLineIsNullOrEmpty() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
        assertThrows(AccessLogParseException.class, () -> parser.parse(""));
    }

    @Test
    void parseAccessLogLineTest_shouldThrowWhenLineIsBadlyFormatted() {
        assertThrows(AccessLogParseException.class, () -> parser.parse("127.0.0.1 - 200 2326"));
        assertThrows(AccessLogParseException.class, () -> parser.parse("/files/apache_pb.gif"));
    }
}