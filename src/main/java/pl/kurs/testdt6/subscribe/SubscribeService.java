package pl.kurs.testdt6.subscribe;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kurs.testdt6.Log.LogService;
import pl.kurs.testdt6.account.AccountEntity;
import pl.kurs.testdt6.account.AccountRepository;
import pl.kurs.testdt6.account.AccountService;
import pl.kurs.testdt6.exception.JobNotFoundException;
import pl.kurs.testdt6.file.FileService;
import pl.kurs.testdt6.job.JobEntity;
import pl.kurs.testdt6.job.JobRepository;
import pl.kurs.testdt6.thread.ThreadService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubscribeService {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final JobRepository jobRepository;
    private final FileService fileService;
    private final ThreadService threadService;
    private final LogService logService;

    public void subscribeJob(JobEntity job) {
        AccountEntity account = accountService.getCurrentlyLoggedUsername();
        account.addJob(job);
        accountRepository.saveAndFlush(account);
    }

    @Transactional
    public String unsubscribeJob(String jobUUID) throws JobNotFoundException, IOException {
        AccountEntity account = accountService.getCurrentlyLoggedUsername();
        JobEntity job = jobRepository.findById(jobUUID).orElseThrow(() -> new JobNotFoundException(jobUUID));
        account.removeJob(job);
        if (job.getAccounts().size() == 0) {
            fileService.deleteFileFromDb(jobUUID);
            jobRepository.delete(job);
            accountRepository.saveAndFlush(account);
            threadService.stopThread(jobUUID);
            logService.saveLog("No more subscribers", job.getPath(), job.getStartTime());
            return "You was the last subscriber. The job has been deleted.";
        }
        accountRepository.saveAndFlush(account);
        return "You has been successfully deleted from subscriber list.";
    }

    @Transactional
    public void deleteAllUsersFromJob(String jobUUID) {
        JobEntity job = jobRepository.findById(jobUUID).orElseThrow(() -> new JobNotFoundException(jobUUID));
        Set<AccountEntity> accounts = job.getAccounts();
        for (AccountEntity account : accounts) {
            account.removeJobFromAllAccounts(job);
        }
        jobRepository.saveAndFlush(job);
    }
}
