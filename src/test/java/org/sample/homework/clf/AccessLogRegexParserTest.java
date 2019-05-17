package org.sample.homework.clf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sample.homework.clf.CommonLogFormatUtils.extractSection;

class AccessLogRegexParserTest {

    @Test
    void extractSection_nominal() {
        assertEquals("/", extractSection("/"));
        assertEquals("/api", extractSection("/api"));
        assertEquals("/api", extractSection("/api/v1"));
        assertEquals("/api", extractSection("/api/v1/users"));
        assertEquals("/", extractSection("/////"));
    }

    @Test
    void extractSection_shouldThrowWhenResourceIsNull() {
        assertThrows(NullPointerException.class, () -> extractSection(null));
    }

    @Test
    void extractSection_shouldThrowWhenResourceIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> extractSection(""));
    }

    @Test
    void extractSection_shouldThrowWhenResourceIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> extractSection("api/v1/posts"));
    }

}