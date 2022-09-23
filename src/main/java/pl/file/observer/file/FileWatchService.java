package pl.file.observer.file;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.file.observer.emailSender.EmailService;
import pl.file.observer.exception.ExceptionService;

import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

@Service
@RequiredArgsConstructor
public class FileWatchService {

    @Value("${filewatch.service.path}")
    private Path directoryPath;
    @Value("#{new Boolean('${filewatch.service.subdirectories}')}")
    private Boolean watchSubdirectories;
    private boolean trace = false;
    @Getter
    private Map<WatchKey, Path> keys;

    private final EmailService emailService;
    private final FileService fileService;
    private final ExceptionService exceptionService;

    @Async
    @EventListener(ApplicationStartedEvent.class)
    public void createWatchServiceTest() throws IOException, InterruptedException, MessagingException {
        Path filePath = Paths.get(String.valueOf(directoryPath));

        WatchService watchService = watchDir(directoryPath, watchSubdirectories);

        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                Path child = resolvePath(key, event);
                if (fileService.isFileObserved(child.toString())) {
                    try {
                        if (Files.exists(child)) {
                            prepareNotification(child, filePath, event);
                        } else {
                            throw new NoSuchFileException(child.toString());
                        }
                    } catch (NoSuchFileException ex) {
                        exceptionService.handleNoSuchFileException(ex);
                    }
                }
                if (watchSubdirectories && (event.kind() == ENTRY_CREATE)) {
                    addNewDirectoryToWatcher(child, watchService);
                }
            }
            key.reset();
        }
    }

    private void addNewDirectoryToWatcher(Path child, WatchService watchService) {
        try {
            if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                registerAll(child, watchService);
            }
        } catch (IOException x) {
            // ignore to keep sample readbale
        }
    }

    private WatchService watchDir(Path dir, boolean watchSubdirectories) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();

        if (watchSubdirectories) {
            registerAll(dir, watchService);
        } else {
            register(dir, watchService);
        }
        this.trace = true;
        return watchService;
    }

    private void register(Path dir, WatchService watcher) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);
        keys.put(key, dir);
    }

    private void registerAll(final Path start, WatchService watcher) throws IOException {
        try {
            Files.walkFileTree(start, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    register(dir, watcher);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (FileSystemException ex) {
            exceptionService.handleFileSystemException(ex);
        }

    }

    private Path resolvePath(WatchKey key, WatchEvent<?> event) {
        Path dir = keys.get(key);
        Path name = (Path) event.context();
        return dir.resolve(name);
    }

    private void prepareNotification(Path child, Path filePath, WatchEvent<?> event) throws IOException, MessagingException {
        String message = null;
        LocalDateTime modificationDateTime = fileService.getModificationDateTime(filePath);
        String addedText = fileService.compareChangesInFile(child.toString());
        if (addedText.isEmpty()) {
            message = "Someone has delete temporary file and we need to create new one. \n" +
                    "all changes will be track from next file edit.";
        } else {
            message = "On " + modificationDateTime.toString() + " file " + event.context() + " has been changed \n" +
                    "Text added to file \n" + addedText;
        }
        emailService.sendNotification(child.toString(), message);
    }
}