package pl.kurs.testdt6.job;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kurs.testdt6.exception.JobNotFoundException;
import pl.kurs.testdt6.file.FileService;
import pl.kurs.testdt6.subscribe.SubscribeService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final FileService fileService;
    private final SubscribeService subscribeService;

    public List<JobEntity> getJobs() {
        return jobRepository.findAll();
    }

    public JobEntity createNewJob(String path) throws IOException {
        if (!isJobAlreadyCreated(path)) {
            JobEntity job = setJobAttrib(path);
            jobRepository.saveAndFlush(job);
            subscribeService.subscribeJob(job);
            return job;
        }
        JobEntity workingJob = jobRepository.findByPath(path);
        subscribeService.subscribeJob(workingJob);
        return workingJob;
    }

    private JobEntity setJobAttrib(String path) throws IOException {
        JobEntity job = new JobEntity();
        job.setPath(path);
        job.setStartTime(LocalDateTime.now());
        job.setLastByte(fileService.getFileBytes(path));
        return job;
    }

    @Transactional
    public String deleteJob(String jobUUID) throws JobNotFoundException {
        return subscribeService.unsubscribeJob(jobUUID);
    }

    private boolean isJobAlreadyCreated(String path) {
        return jobRepository.existsByPath(path);
    }

    public JobEntity getJob(String uuid) {
        return jobRepository.findById(uuid).orElseThrow(() -> new JobNotFoundException(uuid));
    }
}
