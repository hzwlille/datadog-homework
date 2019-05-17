package org.sample.homework.reader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.io.IOException;

/**
 * An abstract class for reading new lines from an actively written-to file.
 */
@RequiredArgsConstructor
@Getter
public abstract class FileWatcher implements Closeable {

    /**
     * The listener to be used to forward new lines.
     */
    private final LineListener listener;

    /**
     * Starts the watcher following changes in the file.
     *
     * @throws IOException if something goes wrong during the process
     */
    public abstract void start() throws IOException;

    /**
     * Allows watchers to complete return.
     *
     * @throws IOException if something goes wrong during the process
     */
    public abstract void stop() throws IOException;

    @Override
    public void close() throws IOException {
        stop();
    }

}
