package pl.file.observer.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.file.observer.job.JobRepository;
import pl.file.observer.job.JobEntity;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
        BasicFileAttributes attr = Files.readAttributes(Path.of(job.getPath()), BasicFileAttributes.class);

        long fileSize = attr.size();
        long fileSizeBeforeEdit = job.getLastByte();

        byte[] testBytes = new byte[(int) (fileSize - fileSizeBeforeEdit)];

        RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
        randomAccessFile.seek(fileSizeBeforeEdit);
        randomAccessFile.readFully(testBytes);
        randomAccessFile.close();

        job.setLastByte(fileSize);
        jobRepository.saveAndFlush(job);

        return new String(testBytes, StandardCharsets.UTF_8);
    }

    public boolean isFileObserved(String path) {
        return jobRepository.existsByPath(path);
    }

    public long getFileBytes(String path) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(Path.of(path), BasicFileAttributes.class);
        return attr.size();
    }
}
