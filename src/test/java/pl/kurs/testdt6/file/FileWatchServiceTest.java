package pl.kurs.testdt6.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class FileWatchServiceTest {

    private WatchService watchService;
    private WatchKey watchKey;
    @TempDir
    Path filePath;


    @BeforeEach
    void setUp() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        watchKey = filePath.register(watchService, ENTRY_MODIFY);
    }

    @Test
    void testEventForFile() throws IOException, InterruptedException {
        File file = new File(String.valueOf(filePath));
        File fileInTempDir = createFileInTempDir(file, "test.txt");

        WatchKey watchTestKey = watchService.poll(10, TimeUnit.SECONDS);
        assertNotNull(watchKey);
        List<WatchEvent<?>> eventList = watchKey.pollEvents();

        addTextToFile(fileInTempDir);
        assertThat(eventList.size()).isEqualTo(1);

        for (WatchEvent event : eventList) {
            assertThat(event.kind() == ENTRY_MODIFY).isEqualTo(true);
            assertThat(event.count()).isEqualTo(1);
        }

        Path eventPath = (Path) eventList.get(0).context();
        assertThat(Files.isSameFile(eventPath, Paths.get("test.txt"))).isEqualTo(true);
        Path watchedPath = (Path) watchTestKey.watchable();
        assertThat(Files.isSameFile(watchedPath, filePath)).isEqualTo(true);
    }


    private File createFileInTempDir(File tempDir, String fileName) throws IOException {
        String content = "Test line in file";
        File testFile = new File(tempDir, fileName);
        Files.write(testFile.toPath(), Collections.singleton(content));
        return testFile;
    }

    private File addTextToFile(File file) throws IOException {
        String addText = "Added Text";
        Files.write(file.toPath(), Collections.singleton(addText));
        return file;
    }
}