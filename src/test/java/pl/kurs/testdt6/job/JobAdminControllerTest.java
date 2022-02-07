package pl.kurs.testdt6.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kurs.testdt6.account.AccountEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/load-data.sql")
class JobAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @TempDir
    File testDirectory;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JobService jobService;

    @Test
    @WithMockUser(username = "Dawid", password = "Test123!", roles = "ADMIN")
    void getJobAdmin() throws Exception {
        JobEntity job = registerTestJob(testDirectory.getPath());
        Set<String> emailSet = job.getAccounts()
                .stream().map(AccountEntity::getEmail)
                .collect(Collectors.toSet());

        MvcResult result = mockMvc.perform(get("/admin/follow/{jobUUID}", job.getJobId()))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        JobStatusAdminModel jobStatusAdminModel = objectMapper.readValue(result.getResponse().getContentAsString(), JobStatusAdminModel.class);
        assertThat(jobStatusAdminModel.getDateTime()).isEqualTo(job.getStartTime());
        assertThat(jobStatusAdminModel.getSubscribersAmount()).isEqualTo(1);
        assertThat(jobStatusAdminModel.getEmailList()).isEqualTo(emailSet);
    }

    private File createFileInTempDir(File tempDir) throws IOException {
        String content = "Test line in file";
        File testFile = new File(tempDir, "text.txt");
        Files.write(testFile.toPath(), Collections.singleton(content));
        return testFile;
    }

    private JobEntity registerTestJob(String testFilePath) throws IOException {
        File fileInTempDir = createFileInTempDir(new File(testFilePath));
        return jobService.createNewJob(fileInTempDir.getPath());
    }
}