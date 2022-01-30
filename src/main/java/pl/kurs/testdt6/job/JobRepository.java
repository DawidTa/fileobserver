package pl.kurs.testdt6.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, String> {

    @Query(value = "SELECT job_id FROM job where path = ?1",
            nativeQuery = true)
    String findIdByPath(String watchPath);

    boolean existsByPath(String path);

    JobEntity findByPath(String path);
}
