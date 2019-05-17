package org.sample.homework.reader;

/**
 * A line listener to be used to get new lines from a {@link FileWatcher}.
 *
 * @see FileWatcher
 */
@FunctionalInterface
public interface LineListener {

    /**
     * Handles a line from a {@link FileWatcher} instance.
     *
     * @param line the new line forwarded by the watcher.
     */
    void handle(String line);
}
