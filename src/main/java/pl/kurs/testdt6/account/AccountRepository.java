package pl.kurs.testdt6.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, String> {
    Optional<AccountEntity> findByUsername(String username);

    @Query(value = "SELECT a.email FROM account a JOIN users_jobs uj ON a.account_id = uj.account_id\n" +
            "JOIN job j on uj.job_id = j.job_id\n" +
            "where j.job_id = ?1",
            nativeQuery = true)
    List<String> findAllBySubscribedJob(String jobId);
}
