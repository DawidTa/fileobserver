package pl.kurs.testdt6.emailSender;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import pl.kurs.testdt6.account.AccountService;
import pl.kurs.testdt6.job.JobRepository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

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


    public void sendNotification(String watchPath, String message) throws MessagingException {
        String jobId = jobRepository.findIdByPath(watchPath);
        List<String> emails = accountService.getSubscribersEmail(jobId);
        for (String email : emails) {
            sendEmail(email, "Notification changes", message);
        }
        System.out.println(emails);
    }
}
