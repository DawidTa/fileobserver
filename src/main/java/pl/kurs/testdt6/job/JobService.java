package pl.kurs.testdt6.job;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kurs.testdt6.account.AccountEntity;
import pl.kurs.testdt6.exception.JobNotFoundException;
import pl.kurs.testdt6.file.FileService;
import pl.kurs.testdt6.subscribe.SubscribeService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final FileService fileService;
    private final SubscribeService subscribeService;

    public List<JobEntity> getJobs() {
        return jobRepository.findAll();
    }

    public CreateJobModel createNewJob(String path) throws IOException {
        CreateJobModel createJobModel = new CreateJobModel();
        if (!isJobAlreadyCreated(path)) {
            JobEntity job = setJobAttrib(path);
            jobRepository.saveAndFlush(job);
            subscribeService.subscribeJob(job);
            createJobModel.setUuid(job.getJobId());
            return createJobModel;
        }
        JobEntity workingJob = jobRepository.findByPath(path);
        subscribeService.subscribeJob(workingJob);
        createJobModel.setUuid(workingJob.getJobId());
        return createJobModel;
    }

    private JobEntity setJobAttrib(String path) throws IOException {
        JobEntity job = new JobEntity();
        job.setPath(path);
        job.setStartTime(LocalDateTime.now());
        job.setLastByte(fileService.getFileBytes(path));
        return job;
    }

    @Transactional
    public String deleteJob(String jobUUID) throws JobNotFoundException, IOException {
        return subscribeService.unsubscribeJob(jobUUID);
    }

    private boolean isJobAlreadyCreated(String path) {
        return jobRepository.existsByPath(path);
    }

    public JobStatusModel getJob(String uuid) {
        JobStatusModel jobStatus = new JobStatusModel();
        JobEntity byId = jobRepository.findById(uuid).orElseThrow(() -> new JobNotFoundException(uuid));
        jobStatus.setDateTime(byId.getStartTime());
        Set<AccountEntity> accounts = byId.getAccounts();
        int count = accounts.size();
        jobStatus.setSubscribersAmount(count);
        return jobStatus;
    }

    public JobStatusAdminModel getJobAdmin(String uuid) {
        JobStatusAdminModel jobStatusAdmin = new JobStatusAdminModel();
        Set<String> emails = new HashSet<>();
        JobEntity byId = jobRepository.findById(uuid).orElseThrow(() -> new JobNotFoundException(uuid));
        jobStatusAdmin.setDateTime(byId.getStartTime());
        Set<AccountEntity> accounts = byId.getAccounts();
        for (AccountEntity account : accounts) {
            emails.add(account.getEmail());
        }
        jobStatusAdmin.setEmailList(emails);
        int count = accounts.size();
        jobStatusAdmin.setSubscribersAmount(count);
        return jobStatusAdmin;
    }
}
