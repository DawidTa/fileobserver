package pl.file.observer.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.file.observer.file.FileService;
import pl.file.observer.job.JobEntity;
import pl.file.observer.job.JobRepository;
import pl.file.observer.log.LogService;
import pl.file.observer.subscribe.SubscribeService;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;

@Service
@RequiredArgsConstructor
public class ExceptionService {

    private final ExceptionModel exceptionModel;
    private final JobRepository jobRepository;
    private final SubscribeService subscribeService;
    private final LogService logService;
    private final FileService fileService;


    public ExceptionModel setExceptionResponse(Exception ex, HttpStatus status) {
        return exceptionModel.setMessage(ex.getMessage())
                .setExceptionType(ex.getClass().getSimpleName())
                .setStatus(status.toString());
    }

    public void handleNoSuchFileException(NoSuchFileException ex) throws IOException {
        JobEntity job = jobRepository.findByPath(ex.getFile());
        subscribeService.deleteAllUsersFromJob(job.getJobId());
        jobRepository.delete(job);
        logService.saveLog("File deleted", ex.getFile(), job.getStartTime());
    }

    public String handleFileSystemException(FileSystemException ex) {
        return ex.getMessage();
    }
}