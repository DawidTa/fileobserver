package pl.file.observer.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.file.observer.validation.PathExists;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobModel {
    @PathExists
    private String path;
}
