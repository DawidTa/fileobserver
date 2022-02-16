package pl.kurs.testdt6.file;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import pl.kurs.testdt6.job.JobEntity;
import pl.kurs.testdt6.job.JobRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class FileService {

    private final JobRepository jobRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    public LocalDateTime getModificationDateTime(Path filePath) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
        return attr.lastModifiedTime()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public String compareChangesInFile(String path) throws IOException {
        JobEntity job = jobRepository.findByPath(path);
        File file = new File(job.getPath());
        byte[] bytes = FileUtils.readFileToByteArray(file);

        byte[] addedContent = Arrays.copyOfRange(bytes, job.getLastByte(), bytes.length);

        job.setLastByte(bytes.length);
        jobRepository.saveAndFlush(job);

        return new String(addedContent);
    }

    public boolean isFileObserved(String path) {
        return jobRepository.existsByPath(path);
    }

    public int getFileBytes(String path) throws IOException {
        File file = new File(path);
        byte[] bytes = FileUtils.readFileToByteArray(file);
        return bytes.length;
    }
}
