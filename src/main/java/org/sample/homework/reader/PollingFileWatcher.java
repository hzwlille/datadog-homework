package org.sample.homework.reader;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;
import java.nio.file.Paths;

/**
 * A {@link FileWatcher} based on {@link Tailer} which is an implementation of the unix "tail -f" functionality.
 * <p>
 * {@link Tailer} is used to follow changes in the file, calling the {@link LineListener}'s handle method for each new line.
 * Note that this implementation handles rotating files correctly.
 */
public class PollingFileWatcher extends FileWatcher {

    /**
     * A {@link TailerListenerAdapter} implementation used to forward new lines from Apache {@link Tailer}.
     */
    @RequiredArgsConstructor
    private static class MyListener extends TailerListenerAdapter {

        private final LineListener lineListener;

        @Override
        public void handle(String line) {
            lineListener.handle(line);
        }
    }

    /**
     * A {@link Tailer} instance used to implement the file watcher.
     */
    private final Tailer tailer;

    /**
     * Creates a {@link FileWatcher} for the given file, with the given poll delay.
     *
     * @param filename        the file to follow for new lines
     * @param listener        the {@link LineListener} to use
     * @param pollDelayMillis the delay between checks of the file for new content in milliseconds
     */
    public PollingFileWatcher(@NonNull String filename, @NonNull LineListener listener, int pollDelayMillis) {
        super(listener);
        File file = Paths.get(filename).toFile();
        // Create a file watcher starting at the end of the file with the given polling delay.
        tailer = new Tailer(file, new MyListener(listener), pollDelayMillis, true);
    }

    @Override
    public void start() {
        tailer.run();
    }

    @Override
    public void stop() {
        tailer.stop();
    }

}
