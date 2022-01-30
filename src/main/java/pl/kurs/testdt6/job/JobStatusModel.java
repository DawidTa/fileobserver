package pl.kurs.testdt6.job;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobStatusModel {
    private LocalDateTime dateTime;
    private int subscribersAmount;
}
