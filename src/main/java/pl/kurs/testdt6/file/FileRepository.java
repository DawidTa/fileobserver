package pl.kurs.testdt6.file;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, String> {

    FileEntity findByJobId(String jobId);

}
