package pl.kurs.testdt6.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
@Sql("/load-data.sql")
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @TempDir
    private File testDirectory;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobRepository jobRepository;


    @Test
    @WithMockUser(username = "Dawid", password = "Test123!", roles = "ADMIN")
    void getAllJobsForAdmin() throws Exception {
        JobEntity job = registerTestJob(testDirectory.getPath(), "test.txt");
        JobEntity nextJob = registerTestJob(testDirectory.getPath(), "test2.txt");

        MvcResult result = mockMvc.perform(get("/follow"))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        List<JobEntity> jobEntity = objectMapper.reader().forType(new TypeReference<List<JobEntity>>() {
        }).readValue(result.getResponse().getContentAsString());
        assertThat(jobEntity.size()).isEqualTo(2);
        assertThat(jobEntity.get(0).getJobId()).isEqualTo(job.getJobId());
        assertThat(jobEntity.get(0).getPath()).isEqualTo(job.getPath());
        assertThat(jobEntity.get(0).getStartTime()).isEqualTo(job.getStartTime());
        assertThat(jobEntity.get(1).getJobId()).isEqualTo(nextJob.getJobId());
        assertThat(jobEntity.get(1).getPath()).isEqualTo(nextJob.getPath());
        assertThat(jobEntity.get(1).getStartTime()).isEqualTo(nextJob.getStartTime());

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
        JobEntity job = registerTestJob(testDirectory.getPath(), "test.txt");

        MvcResult result = mockMvc.perform(get("/follow/{jobUUID}", job.getJobId()))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        JobStatusModel jobStatusModel = objectMapper.readValue(result.getResponse().getContentAsString(), JobStatusModel.class);
        assertThat(jobStatusModel.getSubscribersAmount()).isEqualTo(1);
        assertThat(jobStatusModel.getDateTime()).isEqualTo(job.getStartTime());
    }

    @Test
    @WithMockUser(username = "Dawid123", password = "Test123!", roles = "USER")
    void getJobByUser_shouldReturnNotFound() throws Exception {
        JobEntity job = registerTestJob(testDirectory.getPath(), "test.txt");

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

        JobEntity job = objectMapper.readValue(result.getResponse().getContentAsString(), JobEntity.class);
        assertThat(job.getJobId()).isNotNull();
        assertThat(job.getPath()).isEqualTo(fileInTempDir.getPath());
        assertThat(job.getStartTime()).isBefore(LocalDateTime.now());
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
        JobEntity jobEntity = registerTestJob(testDirectory.getPath(), "test.txt");

        mockMvc.perform(delete("/follow/{jobUUID}", jobEntity.getJobId()))
                .andDo(print())
                .andExpect(status().isOk());

        assertThat(jobRepository.findById(jobEntity.getJobId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "Dawid", password = "Test123!", roles = "ADMIN")
    void deleteJobByAdmin_shouldReturnForbidden() throws Exception {
        JobEntity jobEntity = registerTestJob(testDirectory.getPath(), "test.txt");

        mockMvc.perform(delete("/follow/{jobUUID}", jobEntity.getJobId()))
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

    private JobEntity registerTestJob(String testFilePath, String fileName) throws MessagingException, IOException, InterruptedException {
        File fileInTempDir = createFileInTempDir(new File(testFilePath), fileName);
        return jobService.createNewJob(fileInTempDir.getPath());
    }
}