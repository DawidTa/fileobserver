package pl.kurs.testdt6.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kurs.testdt6.Log.LogService;
import pl.kurs.testdt6.file.FileService;
import pl.kurs.testdt6.job.JobEntity;
import pl.kurs.testdt6.job.JobRepository;
import pl.kurs.testdt6.subscribe.SubscribeService;
import pl.kurs.testdt6.thread.ThreadService;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

@Service
@RequiredArgsConstructor
public class ExceptionService {

    private final ExceptionModel exceptionModel;
    private final ThreadService threadService;
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
        threadService.stopThread(job.getJobId());
        subscribeService.deleteAllUsersFromJob(job.getJobId());
        fileService.deleteFileFromDb(job.getJobId());
        jobRepository.delete(job);
        logService.saveLog("File deleted", ex.getFile(), job.getStartTime());
    }

    public void handleClosedWatchServiceException(String watchPath) {

    }
}