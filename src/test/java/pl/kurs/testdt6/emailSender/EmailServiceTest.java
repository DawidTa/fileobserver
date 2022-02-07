package pl.kurs.testdt6.emailSender;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class EmailServiceTest {

    private final GreenMail smtp =
            new GreenMail(new ServerSetup(3025,"127.0.0.1", ServerSetup.PROTOCOL_SMTP));

    @Autowired
    private JavaMailSender javaMailSender;


    @BeforeEach
    void setUp() {
        smtp.setUser("username", "secret");
        smtp.start();
    }

    @AfterEach
    void after() {
        smtp.stop();
    }

    @Test
    void sendEmail() throws Throwable {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject("Testowy email");
        mailMessage.setText("Testowa wiadomosc email");
        mailMessage.setTo("test@test.pl");
        javaMailSender.send(mailMessage);


        MimeMessage[] receivedMessage = smtp.getReceivedMessages();
        assertEquals(1, receivedMessage.length);

        MimeMessage currentMessage = receivedMessage[0];

        assertEquals("Testowy email", currentMessage.getSubject());
        assertEquals("test@test.pl", currentMessage.getAllRecipients()[0].toString());
        assertEquals("Testowa wiadomosc email", GreenMailUtil.getBody(currentMessage));


    }

}