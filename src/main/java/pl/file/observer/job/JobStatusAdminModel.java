package pl.file.observer.job;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class JobStatusAdminModel {
    private LocalDateTime dateTime;
    private int subscribersAmount;
    private Set<String> emailList;
}
