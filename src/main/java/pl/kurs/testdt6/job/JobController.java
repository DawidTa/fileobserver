package pl.kurs.testdt6.job;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.kurs.testdt6.exception.JobNotFoundException;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
@Transactional
public class JobController {

    private final JobService jobService;

    @GetMapping
    @ApiOperation(value = "Finds all active jobs",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = JobEntity.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal Sever Error")
    })
    public ResponseEntity getAllJobs() {
        return ResponseEntity.ok(jobService.getJobs());
    }

    @GetMapping("/{jobUUID}")
    @ApiOperation(value = "Find specific active job and number of observers",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok", response = JobEntity.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Sever Error")
    })
    public ResponseEntity getJob(@PathVariable String jobUUID, Authentication authentication) {
        String role = authentication.getAuthorities().toString();
        if (role.equals("[ROLE_USER]")) {
            return new ResponseEntity(jobService.getJob(jobUUID), HttpStatus.OK);
        }
            return new ResponseEntity(jobService.getJobAdmin(jobUUID), HttpStatus.OK);
    }

    @PostMapping
    @ApiOperation(value = "Start observing the file",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", response = JobEntity.class),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 500, message = "Internal Sever Error")
    })
    public ResponseEntity registerJob(@Valid @RequestBody JobModel jobModel) throws IOException {
        return new ResponseEntity(jobService.createNewJob(jobModel.getPath()), HttpStatus.CREATED);
    }

    @DeleteMapping("/{jobUUID}")
    @ApiOperation(value = "End of file observation",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 500, message = "Internal Sever Error")
    })
    public ResponseEntity deleteJob(@PathVariable String jobUUID) throws JobNotFoundException, IOException {
        return new ResponseEntity(jobService.deleteJob(jobUUID), HttpStatus.OK);
    }
}
