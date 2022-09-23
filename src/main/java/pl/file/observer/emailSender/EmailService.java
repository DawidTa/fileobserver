package pl.file.observer.emailSender;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.file.observer.account.AccountEntity;
import pl.file.observer.account.AccountService;
import pl.file.observer.job.JobRepository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final JobRepository jobRepository;
    private final AccountService accountService;

    public void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(content);
        javaMailSender.send(mimeMessage);
    }

    @Async
    public void sendNotification(String watchPath, String message) throws MessagingException {
        String jobId = jobRepository.findIdByPath(watchPath);
        List<AccountEntity> accountList = accountService.getSubscribersEmail(jobId);
        Set<String> emails = accountList.stream().map(AccountEntity::getEmail).collect(Collectors.toSet());
        for (String email : emails) {
            sendEmail(email, "Notification changes", message);
        }
    }
}
