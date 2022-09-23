package pl.file.observer.subscribe;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.file.observer.account.AccountEntity;
import pl.file.observer.account.AccountRepository;
import pl.file.observer.account.AccountService;
import pl.file.observer.exception.JobNotFoundException;
import pl.file.observer.job.JobEntity;
import pl.file.observer.job.JobRepository;
import pl.file.observer.log.LogService;

import javax.transaction.Transactional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubscribeService {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final JobRepository jobRepository;
    private final LogService logService;

    public void subscribeJob(JobEntity job) {
        AccountEntity account = accountService.getCurrentlyLoggedUsername();
        account.addJob(job);
        accountRepository.saveAndFlush(account);
    }

    @Transactional
    public String unsubscribeJob(String jobUUID) throws JobNotFoundException {
        AccountEntity account = accountService.getCurrentlyLoggedUsername();
        JobEntity job = jobRepository.findById(jobUUID).orElseThrow(() -> new JobNotFoundException(jobUUID));
        account.removeJob(job);
        if (job.getAccounts().size() == 0) {
            jobRepository.delete(job);
            accountRepository.saveAndFlush(account);
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
