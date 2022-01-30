package pl.kurs.testdt6.thread;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kurs.testdt6.file.FileModel;

import java.io.IOException;
import java.nio.file.WatchService;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ThreadService {

    private final FileModel fileModel;

    public void stopThread(String uuid) throws IOException {
        Map<String, WatchService> watchServiceMap = fileModel.getWatchServices();
        for (String key : watchServiceMap.keySet()) {
            if (key.equals(uuid)) {
                WatchService watchService = watchServiceMap.get(uuid);
                watchService.close();
            }
        }
        watchServiceMap.remove(uuid);
    }
}
