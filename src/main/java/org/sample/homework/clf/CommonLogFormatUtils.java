package org.sample.homework.clf;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * An utility class which contains Common Log Format related helper functions.
 */
@UtilityClass
public class CommonLogFormatUtils {

    /**
     * Returns <tt>null</tt> if the string provided is equal to "-".
     *
     * @param str the string to process
     * @return str if the content is not equal to "-", <tt>null</tt> otherwise
     */
    static String nullifyIfDash(@NonNull String str) {
        if ("-".equals(str)) {
            return null;
        }

        return str;
    }

    /**
     * Parses the content size from the Common Log Format log line.
     *
     * @param str the content size extracted from the log line
     * @return O if a dash is provided, the parsed int otherwise
     */
    static int parseContentSize(@NonNull String str) {
        // If the size is not provided, there is a dash instead.
        if ("-".equals(str)) {
            return 0;
        }

        return Integer.parseInt(str);
    }

    /**
     * Returns the section given a string corresponding to a requested resource.
     * A section is defined as being what's before the second '/' in a URL (the section for "/pages/create' is "/pages").
     *
     * @param resource the resource from which to extract the section, not <tt>null</tt>
     * @return the extracted section, not <tt>null</tt>
     * @throws IllegalArgumentException in case of badly formatted resource string
     */
    public static String extractSection(@NonNull String resource) {
        // All request resources should start with a backslash.
        if (resource.isEmpty() || resource.charAt(0) != '/') {
            throw new IllegalArgumentException("Badly formatted resource²: " + resource + '!');
        }

        // Try to find a second backslash (starting from index 1).
        int index = resource.indexOf('/', 1);

        // Resource contains exactly one backslash at the beginning.
        if (index == -1) {
            return resource;
        }

        // Extract the section.
        return resource.substring(0, index);
    }

}
