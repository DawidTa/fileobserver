package pl.file.observer.job;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.file.observer.exception.JobNotFoundException;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
@Transactional
public class JobController {

    private final JobService jobService;
    private final ModelMapper modelMapper;

    @GetMapping
    @ApiOperation(value = "Finds all active jobs",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = JobEntity.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal Sever Error")
    })
    public ResponseEntity getAllJobs() {
        TypeToken<List<JobDTOModel>> typeToken = new TypeToken<>() {
        };
        List<JobDTOModel> jobDTOModel = modelMapper.map(jobService.getJobs(), typeToken.getType());
        return ResponseEntity.ok(jobDTOModel);
    }

    @GetMapping("/{jobUUID}")
    @ApiOperation(value = "Find specific active job and number of observers",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok", response = JobStatusModel.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Sever Error")
    })
    public ResponseEntity getJob(@PathVariable String jobUUID) {
        String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
        if (role.contains("ADMIN")) {
            JobStatusAdminModel jobStatusAdmin = modelMapper.map(jobService.getJob(jobUUID), JobStatusAdminModel.class);
            return new ResponseEntity(jobStatusAdmin, HttpStatus.OK);
        }
        JobStatusModel jobStatusModel = modelMapper.map(jobService.getJob(jobUUID), JobStatusModel.class);
        return new ResponseEntity(jobStatusModel, HttpStatus.OK);
    }

    @PostMapping
    @ApiOperation(value = "Start observing the file",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", response = CreateJobModel.class),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 500, message = "Internal Sever Error")
    })
    public ResponseEntity registerJob(@Valid @RequestBody JobModel jobModel) throws IOException {
        CreateJobModel createJobModel = modelMapper.map(jobService.createNewJob(jobModel.getPath()), CreateJobModel.class);
        return new ResponseEntity(createJobModel, HttpStatus.CREATED);
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
    public ResponseEntity deleteJob(@PathVariable String jobUUID) throws JobNotFoundException {
        return new ResponseEntity(jobService.deleteJob(jobUUID), HttpStatus.OK);
    }
}
