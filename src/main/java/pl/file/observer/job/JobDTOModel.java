package pl.file.observer.job;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobDTOModel {

    private String jobId;
    private String path;
    private LocalDateTime startTime;
    private long lastByte;
}
