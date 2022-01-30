package pl.kurs.testdt6.job;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/follow")
public class JobAdminController {

    private final JobService jobService;

    @GetMapping("/{jobUUID}")
    public ResponseEntity getJobAdmin(@PathVariable String jobUUID) {
        return new ResponseEntity(jobService.getJobAdmin(jobUUID), HttpStatus.OK);
    }
}
