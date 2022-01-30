package pl.kurs.testdt6.file;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.kurs.testdt6.emailSender.EmailService;
import pl.kurs.testdt6.exception.ExceptionService;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FileWatchService {

    private final EmailService emailService;
    private final FileService fileService;
    private final FileModel fileModel;
    private final ExceptionService exceptionService;


    @Async
    public void createWatchService(String watchPath, String uuid) throws InterruptedException, IOException, MessagingException {
        Path filePath = Paths.get(watchPath);
        Path parent = filePath.getParent();
        WatchService watchService
                = FileSystems.getDefault().newWatchService();

        parent.register(
                watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);
        fileModel.getWatchServices().put(uuid, watchService);

        setNameForWatchService(uuid);

        notifyChanges(watchService, parent, filePath, watchPath);
    }

    @Transactional
    public void notifyChanges(WatchService watchService, Path parent, Path filePath, String watchPath) throws InterruptedException, IOException, MessagingException {
        String message = null;
        WatchKey key;
        try {
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    System.out.println(filePath.toString());
                    Path changed = parent.resolve((Path) event.context());
                    try {
                        if (Files.exists(changed) && Files.isSameFile(changed, filePath)) {
                            LocalDateTime modificationDateTime = fileService.getModificationDateTime(filePath);
                            String addedText = fileService.addedContentToFile(watchPath);
                            message = "On " + modificationDateTime.toString() + " file " + event.context() + " has been changed \n" +
                                    "Text added to file \n" + addedText;
                            emailService.sendNotification(watchPath, message);
                        }
                    } catch (NoSuchFileException ex) {
                        exceptionService.handleNoSuchFileException(ex);
                    }

                }
                key.reset();
            }
        } catch (ClosedWatchServiceException ex) {
            exceptionService.handleClosedWatchServiceException(watchPath);
        }

    }

    private void setNameForWatchService(String uuid) {
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread thread : threads) {
            if (thread.getName().equals("FileSystemWatcher")) {
                thread.setName(uuid);
            }
        }
    }
}