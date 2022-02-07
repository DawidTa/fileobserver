package pl.kurs.testdt6.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.kurs.testdt6.validation.PathExists;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobModel {
    @PathExists
    private String path;
}
