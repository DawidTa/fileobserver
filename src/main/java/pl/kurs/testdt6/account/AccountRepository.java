package pl.kurs.testdt6.account;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, String> {
    @EntityGraph(attributePaths = "roles")
    Optional<AccountEntity> findByUsername(String username);

    List<AccountEntity> findAllByJobs_JobId(String jobId);
}
