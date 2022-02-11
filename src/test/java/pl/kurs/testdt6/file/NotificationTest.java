package pl.kurs.testdt6.file;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import pl.kurs.testdt6.job.JobEntity;
import pl.kurs.testdt6.job.JobService;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Sql("/load-data.sql")
public class NotificationTest {

    @TempDir
    File testDirectory;
    @Autowired
    private JobService jobService;
    @Autowired
    private FileWatchService fileWatchService;
    @Autowired
    private JavaMailSender javaMailSender;

    private final GreenMail smtp =
            new GreenMail(new ServerSetup(3025, "127.0.0.1", ServerSetup.PROTOCOL_SMTP));


    @BeforeEach
    void setUp() throws IOException {
        smtp.setUser("username", "secret");
        smtp.start();
    }

    @AfterEach
    void after() {
        smtp.stop();
    }

    @Test
    @WithMockUser(username = "Dawid123", password = "Test123!", roles = "USER")
    void registerJob_sendMailNotificationChanges() throws IOException, InterruptedException, MessagingException {
        TimeUnit.SECONDS.sleep(10);
        createBigFileInTempDir(new File(testDirectory.getPath()), "test.txt");
        String path = testDirectory.getPath() + "/test.txt";


        JobEntity job = registerTestJobBigFile(path);
        addManyLinesToFile(Path.of(job.getPath()));


        TimeUnit.SECONDS.sleep(10);

        MimeMessage[] receivedMessage = smtp.getReceivedMessages();
        assertEquals(1, receivedMessage.length);


        MimeMessage currentMessage = receivedMessage[0];

        assertEquals("Notification changes", currentMessage.getSubject());
        assertTrue(GreenMailUtil.getBody(currentMessage).contains("Added Text 99999"));
        assertEquals("dawid.taczkowski@gmail.com", currentMessage.getAllRecipients()[0].toString());
    }


    private JobEntity registerTestJobBigFile(String path) throws IOException {
        return jobService.createNewJob(path);
    }

    private void addTextToFile(Path file) throws IOException {
        String addText = "Added Text";
        Files.write(file, (addText + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    }

    private File createBigFileInTempDir(File tempDir, String fileName) throws IOException {
        int i = 1;
        File testFile = new File(tempDir, fileName);
        for (int j = 0; j < 1000000; j++) {
            String content = "Test line in file number " + i;
            Files.write(testFile.toPath(), (content + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            i++;
        }
        return testFile;
    }

    private void addManyLinesToFile(Path file) throws IOException {
        int i = 1;
        for (int j = 0; j < 100000; j++) {
            String addText = "Added Text " + i;
            Files.write(file, (addText + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            i++;
        }

    }
}
