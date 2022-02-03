package pl.kurs.testdt6.Log;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<LogEntity, String> {
}
