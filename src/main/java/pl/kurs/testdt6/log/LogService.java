package pl.kurs.testdt6.log;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    public void saveLog(String operationType, String path, LocalDateTime startTime) {
        LogEntity logEntity = new LogEntity();
        logEntity.setOperationType(operationType)
                .setPath(path)
                .setJobStartDate(startTime)
                .setJobEndDate(LocalDateTime.now());
        logRepository.saveAndFlush(logEntity);
    }
}
