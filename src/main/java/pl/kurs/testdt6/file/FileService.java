package pl.kurs.testdt6.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kurs.testdt6.exception.JobNotFoundException;
import pl.kurs.testdt6.job.JobEntity;
import pl.kurs.testdt6.job.JobRepository;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
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

    public void saveTempFilePathToDb(String path, String jobId) {
        FileEntity tempFile = new FileEntity();
        tempFile.setTempFilePath(path)
                .setJobId(jobId);

        fileRepository.saveAndFlush(tempFile);
    }

    public String compareChangesInFile(String path) throws IOException {
        JobEntity job = jobRepository.findByPath(path);
        FileEntity tempFile = fileRepository.findByJobId(job.getJobId());

        if (!Files.exists(Paths.get(tempFile.getTempFilePath()))) {
            createTempFileAfterDeleted(tempFile, path);
        }

        List<String> diff = new ArrayList<>();

        BufferedReader tempFileBf = Files.newBufferedReader(Path.of(tempFile.getTempFilePath()));
        BufferedReader observedFileBf = Files.newBufferedReader(Path.of(job.getPath()));

        long lineNumber = 1;
        String line1 = "", line2 = "";
        while ((line1 = observedFileBf.readLine()) != null) {
            line2 = tempFileBf.readLine();
            if (line2 == null || !line1.equals(line2)) {
                diff.add(line1);
            }
            lineNumber++;
        }

        String join = String.join("\n", diff);

        addTextToTemp(join, tempFile.getTempFilePath());

        return join;
    }

    @Transactional
    public void deleteFileFromDb(String jobUUID) {
        JobEntity job = jobRepository.findById(jobUUID).orElseThrow(() -> new JobNotFoundException(jobUUID));
        FileEntity file = fileRepository.findByJobId(job.getJobId());
        fileRepository.delete(file);
    }

    public void deleteTempFile(String jobUUID) throws IOException {
        FileEntity file = fileRepository.findByJobId(jobUUID);
        Files.delete(Paths.get(file.getTempFilePath()));
    }

    public boolean isFileObserved(String path) {
        return jobRepository.existsByPath(path);
    }

    public void createTempFile(String path, String jobId) throws IOException {
        Path tempFile = Files.createTempFile(null, null);

        Path filePath = Paths.get(path);
        Files.write(tempFile, Collections.singleton(Files.readString(filePath)));

        saveTempFilePathToDb(tempFile.toString(), jobId);
    }

    private void createTempFileAfterDeleted(FileEntity tempFile, String path) throws IOException {
        Path newTempFile = Files.createTempFile(null, null);

        Path filePath = Paths.get(path);
        Files.write(newTempFile, Collections.singleton(Files.readString(filePath)));

        tempFile.setTempFilePath(newTempFile.toString());
        fileRepository.saveAndFlush(tempFile);
    }

    private void addTextToTemp(String difference, String tempFilePath) throws IOException {
        Files.write(Paths.get(tempFilePath),
                difference.getBytes(),
                StandardOpenOption.APPEND);
    }
}
