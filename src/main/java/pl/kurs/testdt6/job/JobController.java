package pl.kurs.testdt6.job;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kurs.testdt6.exception.JobNotFoundException;

import javax.mail.MessagingException;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity getAllJobs() {
        return ResponseEntity.ok(jobService.getJobs());
    }

    @GetMapping("/{jobUUID}")
    public ResponseEntity getJob(@PathVariable String jobUUID) {
        return new ResponseEntity(jobService.getJob(jobUUID), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity registerJob(@RequestBody JobModel jobModel) throws IOException, MessagingException, InterruptedException {
        return new ResponseEntity(jobService.createNewJob(jobModel.getPath()), HttpStatus.CREATED);
    }

    @DeleteMapping("/{jobUUID}")
    public ResponseEntity deleteJob(@PathVariable String jobUUID) throws JobNotFoundException, IOException {
        return new ResponseEntity(jobService.deleteJob(jobUUID), HttpStatus.OK);
    }
}
