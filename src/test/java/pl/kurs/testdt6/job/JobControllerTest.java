package pl.kurs.testdt6.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kurs.testdt6.account.AccountEntity;
import pl.kurs.testdt6.file.FileWatchService;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchKey;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/load-data.sql")
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @TempDir
    File testDirectory;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JobService jobService;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private FileWatchService fileWatchService;
    private Map<WatchKey, Path> keys;
    private File fileInTempDir;


    private final GreenMail smtp =
            new GreenMail(new ServerSetup(3025, "127.0.0.1", ServerSetup.PROTOCOL_SMTP));

    @BeforeEach
    void setUp() throws IOException {
        smtp.setUser("username", "secret");
        smtp.start();
        fileInTempDir = createFileInTempDir(testDirectory, "test.txt");
        keys = this.fileWatchService.getKeys();
    }

    @AfterEach
    void after() {
        smtp.stop();
    }


    @Test
    @WithMockUser(username = "Dawid", password = "Test123!", roles = "ADMIN")
    void getAllJobsForAdmin() throws Exception {

        CreateJobModel job = registerTestJob(testDirectory.getPath(), "test.txt");
        CreateJobModel nextJob = registerTestJob(testDirectory.getPath(), "test2.txt");

        JobEntity fistJob = jobRepository.getById(job.getUuid());
        JobEntity secondJob = jobRepository.getById(nextJob.getUuid());

        MvcResult result = mockMvc.perform(get("/follow"))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        List<JobEntity> jobEntity = objectMapper.reader().forType(new TypeReference<List<JobEntity>>() {
        }).readValue(result.getResponse().getContentAsString());
        assertThat(jobEntity.size()).isEqualTo(2);
        assertThat(jobEntity.get(0).getJobId()).isEqualTo(fistJob.getJobId());
        assertThat(jobEntity.get(0).getPath()).isEqualTo(fistJob.getPath());
        assertThat(jobEntity.get(0).getStartTime()).isEqualTo(fistJob.getStartTime());
        assertThat(jobEntity.get(1).getJobId()).isEqualTo(secondJob.getJobId());
        assertThat(jobEntity.get(1).getPath()).isEqualTo(secondJob.getPath());
        assertThat(jobEntity.get(1).getStartTime()).isEqualTo(secondJob.getStartTime());

    }

    @Test
    @WithMockUser(username = "Dawid123", password = "Test123!", roles = "USER")
    void getAllJobsForUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/follow"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "Dawid123", password = "Test123!", roles = "USER")
    void getJobByUser() throws Exception {
        CreateJobModel job = registerTestJob(testDirectory.getPath(), "test.txt");

        MvcResult result = mockMvc.perform(get("/follow/{jobUUID}", job.getUuid()))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        JobStatusModel jobStatusModel = objectMapper.readValue(result.getResponse().getContentAsString(), JobStatusModel.class);
        assertThat(jobStatusModel.getSubscribersAmount()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "Dawid123", password = "Test123!", roles = "USER")
    void getJobByUser_shouldReturnNotFound() throws Exception {
        CreateJobModel job = registerTestJob(testDirectory.getPath(), "test.txt");

        mockMvc.perform(get("/follow/{jobUUID}", "c1950e0a-8202-11ec-a8a3-0242ac120002"))
                .andDo(print())
                .andExpect(jsonPath("$.message").value("Job with provided id: c1950e0a-8202-11ec-a8a3-0242ac120002 not exists or id is invalid"))
                .andExpect(jsonPath("$.exceptionType").value("JobNotFoundException"))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "Dawid123", password = "Test123!", roles = "USER")
    void registerJob() throws Exception {
        File fileInTempDir = createFileInTempDir(testDirectory, "test.txt");
        JobModel jobModel = new JobModel(fileInTempDir.getPath());
        String jsonContent = objectMapper.writeValueAsString(jobModel);

        MvcResult result = mockMvc.perform(post("/follow")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn();

        CreateJobModel job = objectMapper.readValue(result.getResponse().getContentAsString(), CreateJobModel.class);
        assertThat(job.getUuid()).isNotNull();
    }

    @Test
    @WithMockUser(username = "Dawid123", password = "Test123!", roles = "USER")
    void registerJob_shouldSendMailNotificationChanges() throws Exception {
        TimeUnit.SECONDS.sleep(10);
        File fileInTempDir = createFileInTempDir(testDirectory, "test.txt");
        JobModel jobModel = new JobModel(fileInTempDir.getPath());
        String jsonContent = objectMapper.writeValueAsString(jobModel);

        MvcResult result = mockMvc.perform(post("/follow")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn();

        CreateJobModel job = objectMapper.readValue(result.getResponse().getContentAsString(), CreateJobModel.class);
        assertThat(job.getUuid()).isNotNull();

        addTextToFile(Path.of(fileInTempDir.getPath()));
        TimeUnit.SECONDS.sleep(20);

        MimeMessage[] receivedMessage = smtp.getReceivedMessages();
        assertEquals(1, receivedMessage.length);


        MimeMessage currentMessage = receivedMessage[0];

        assertEquals("Notification changes", currentMessage.getSubject());
        assertTrue(GreenMailUtil.getBody(currentMessage).contains("Added Text"));
        assertEquals("dawid.taczkowski@gmail.com", currentMessage.getAllRecipients()[0].toString());
    }

    @Test
    @WithMockUser(username = "Dawid123", password = "Test123!", roles = "USER")
    void registerJob_shouldSendMailNotificationChanges_BigFile() throws Exception {
        TimeUnit.SECONDS.sleep(10);

        File fileInTempDir = createFileInTempDir(testDirectory, "test.txt");
        JobModel jobModel = new JobModel(fileInTempDir.getPath());
        String jsonContent = objectMapper.writeValueAsString(jobModel);

        MvcResult result = mockMvc.perform(post("/follow")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn();

        CreateJobModel job = objectMapper.readValue(result.getResponse().getContentAsString(), CreateJobModel.class);
        assertThat(job.getUuid()).isNotNull();

        addManyLinesToFile(Path.of(fileInTempDir.getPath()));
        TimeUnit.SECONDS.sleep(20);

        MimeMessage[] receivedMessage = smtp.getReceivedMessages();
        assertEquals(1, receivedMessage.length);

        MimeMessage currentMessage = receivedMessage[0];

        assertEquals("Notification changes", currentMessage.getSubject());
        assertTrue(GreenMailUtil.getBody(currentMessage).contains("Added Text 9999"));
        assertEquals("dawid.taczkowski@gmail.com", currentMessage.getAllRecipients()[0].toString());
    }


    @Test
    void registerJob_shouldSendMailNotificationChangesTwoUsers() throws Exception {
        TimeUnit.SECONDS.sleep(10);
        JobModel jobModel = new JobModel(fileInTempDir.getPath());
        String jsonContent = objectMapper.writeValueAsString(jobModel);

        mockMvc.perform(post("/follow")
                        .with(user("Dawid123").password("Test123!").roles("USER"))
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());

        mockMvc.perform(post("/follow")
                        .with(user("DawidUser").password("Test123!").roles("USER"))
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());

        addTextToFile(Path.of(fileInTempDir.getPath()));
        TimeUnit.SECONDS.sleep(20);

        MimeMessage[] receivedMessage = smtp.getReceivedMessages();
        assertEquals(2, receivedMessage.length);


        MimeMessage currentMessage = receivedMessage[0];
        MimeMessage secondMessage = receivedMessage[1];

        assertEquals("Notification changes", currentMessage.getSubject());
        assertTrue(GreenMailUtil.getBody(currentMessage).contains("Added Text"));
        assertEquals("dtmaly7@gmail.com", currentMessage.getAllRecipients()[0].toString());
        assertEquals("dawid.taczkowski@gmail.com", secondMessage.getAllRecipients()[0].toString());

    }

    @Test
    void unsubscribeJobByOneUser() throws Exception {
        File fileInTempDir = createFileInTempDir(testDirectory, "test.txt");
        JobModel jobModel = new JobModel(fileInTempDir.getPath());
        String jsonContent = objectMapper.writeValueAsString(jobModel);

        mockMvc.perform(post("/follow")
                        .with(user("Dawid123").password("Test123!").roles("USER"))
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/follow")
                        .with(user("DawidUser").password("Test123!").roles("USER"))
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn();

        JobEntity job = objectMapper.readValue(result.getResponse().getContentAsString(), JobEntity.class);

        MvcResult resultTwoSubs = mockMvc.perform(get("/follow/{jobUUID}", job.getJobId())
                        .with(user("Dawid123").password("Test123!").roles("USER")))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        JobStatusModel jobStatusModelSub = objectMapper.readValue(resultTwoSubs.getResponse().getContentAsString(), JobStatusModel.class);
        assertThat(jobStatusModelSub.getSubscribersAmount()).isEqualTo(2);
        assertThat(jobStatusModelSub.getDateTime()).isEqualTo(job.getStartTime());


        mockMvc.perform(delete("/follow/{jobUUID}", job.getJobId())
                        .with(user("DawidUser").password("Test123!").roles("USER")))
                .andDo(print());


        MvcResult resultUnsub = mockMvc.perform(get("/follow/{jobUUID}", job.getJobId())
                        .with(user("Dawid123").password("Test123!").roles("USER")))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        JobStatusModel jobStatusModelUnsub = objectMapper.readValue(resultUnsub.getResponse().getContentAsString(), JobStatusModel.class);
        assertThat(jobStatusModelUnsub.getSubscribersAmount()).isEqualTo(1);
        assertThat(jobStatusModelUnsub.getDateTime()).isEqualTo(job.getStartTime());


    }

    @Test
    @WithMockUser(username = "Dawid", password = "Test123!", roles = "ADMIN")
    void getJobAdmin() throws Exception {
        CreateJobModel job = registerTestJob(testDirectory.getPath(), "test.txt");
        JobEntity jobEntity = jobRepository.getById(job.getUuid());
        Set<String> emailSet = jobEntity.getAccounts()
                .stream().map(AccountEntity::getEmail)
                .collect(Collectors.toSet());

        MvcResult result = mockMvc.perform(get("/follow/{jobUUID}", job.getUuid()))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        JobStatusAdminModel jobStatusAdminModel = objectMapper.readValue(result.getResponse().getContentAsString(), JobStatusAdminModel.class);
        assertThat(jobStatusAdminModel.getSubscribersAmount()).isEqualTo(1);
        assertThat(jobStatusAdminModel.getEmailList()).isEqualTo(emailSet);
    }

    @Test
    @WithMockUser(username = "Dawid123", password = "Test123!", roles = "USER")
    void registerJob_shouldReturnBadRequest() throws Exception {
        File fileInTempDir = createFileInTempDir(testDirectory, "test.txt");
        JobModel jobModel = new JobModel("Test2.txt");
        String jsonContent = objectMapper.writeValueAsString(jobModel);

        mockMvc.perform(post("/follow")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.message").value("File not exist or is a directory"))
                .andExpect(jsonPath("$.exceptionType").value("IllegalArgumentException"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void registerJob_shouldUnauthorized() throws Exception {
        File fileInTempDir = createFileInTempDir(testDirectory, "test.txt");
        JobModel jobModel = new JobModel("Test2.txt");
        String jsonContent = objectMapper.writeValueAsString(jobModel);

        mockMvc.perform(post("/follow")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "Dawid123", password = "Test123!", roles = "USER")
    void deleteJob() throws Exception {
        CreateJobModel jobEntity = registerTestJob(testDirectory.getPath(), "test.txt");

        mockMvc.perform(delete("/follow/{jobUUID}", jobEntity.getUuid()))
                .andDo(print())
                .andExpect(status().isOk());

        assertThat(jobRepository.findById(jobEntity.getUuid())).isEmpty();
    }

    @Test
    @WithMockUser(username = "Dawid", password = "Test123!", roles = "ADMIN")
    void deleteJobByAdmin_shouldReturnForbidden() throws Exception {
        CreateJobModel createJobModel = registerTestJob(testDirectory.getPath(), "test.txt");

        mockMvc.perform(delete("/follow/{jobUUID}", createJobModel.getUuid()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void deleteJobByAdmin_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/follow/{jobUUID}", "c1950e0a-8202-11ec-a8a3-0242ac120002"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }


    private File createFileInTempDir(File tempDir, String fileName) throws IOException {
        String content = "Test line in file";
        File testFile = new File(tempDir, fileName);
        Files.write(testFile.toPath(), Collections.singleton(content));
        return testFile;
    }


    private CreateJobModel registerTestJob(String testFilePath, String fileName) throws IOException {
        File fileInTempDir = createFileInTempDir(new File(testFilePath), fileName);
        return jobService.createNewJob(fileInTempDir.getPath());
    }

    private void addTextToFile(Path file) throws IOException {
        String addText = "Added Text";
        Files.write(file, (addText + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    }

    private void addManyLinesToFile(Path file) throws IOException {
        int i = 1;
        for (int j = 0; j < 10000; j++) {
            String addText = "Added Text " + i;
            Files.write(file, (addText + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            i++;
        }

    }

}