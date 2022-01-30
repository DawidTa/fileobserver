package pl.kurs.testdt6.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import pl.kurs.testdt6.account.AccountEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "job")
public class JobEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String jobId;
    private String path;
    private LocalDateTime startTime;

    @JsonIgnore
    @ManyToMany(mappedBy = "jobs", fetch = FetchType.EAGER)
    private Set<AccountEntity> accounts = new HashSet<>();
}
