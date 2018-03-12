package model.monitor;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Implementation from <a href="https://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java">Oracle</a>
 */
public class WatchDir {

    private WatchService watchService;
    private Map<WatchKey, Path> keys;
    private boolean recursive;
    private boolean trace = false;

    private void registerAll(final Path start) {
        try {
            Files.walkFileTree(start, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void register(Path path) {
        try {
            WatchKey watchKey = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            if (trace) {
                Path previous = keys.get(watchKey);
                if(previous != null) {
                    if(path.equals())
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
