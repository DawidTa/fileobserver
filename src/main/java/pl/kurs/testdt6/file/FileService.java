package pl.kurs.testdt6.file;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pl.kurs.testdt6.exception.JobNotFoundException;
import pl.kurs.testdt6.job.JobEntity;
import pl.kurs.testdt6.job.JobRepository;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final JobRepository jobRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    public boolean isFileExist(String path) {
        File file = new File(path);
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException("File not exist or is a directory");
        }
        return true;
    }

    public LocalDateTime getModificationDateTime(Path filePath) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
        return attr.lastModifiedTime()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public void saveFileToDb(String path) throws IOException {
        FileEntity file = new FileEntity();
        file.setFilePath(path);

        Path filePath = Paths.get(path);
        file.setFileContent(Files.readString(filePath));
        fileRepository.saveAndFlush(file);
    }

    public String addedContentToFile(String path) throws IOException {
        FileEntity file = fileRepository.findByFilePath(path);
        String fileContent = file.getFileContent();
        String fileAddedContent = Files.readString(Path.of(path));
        file.setFileContent(fileAddedContent);
        fileRepository.saveAndFlush(file);
        return StringUtils.difference(fileContent, fileAddedContent);

    }

    @Transactional
    public void deleteFileFromDb(String jobUUID) {
        JobEntity job = jobRepository.findById(jobUUID).orElseThrow(() -> new JobNotFoundException(jobUUID));
        FileEntity file = fileRepository.findByFilePath(job.getPath());
        fileRepository.delete(file);
    }
}
