package pl.kurs.testdt6.job;

import lombok.Data;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class JobStatusAdminModel {
    private LocalDateTime dateTime;
    private int subscribersAmount;
    private Set<String> emailList;
}
