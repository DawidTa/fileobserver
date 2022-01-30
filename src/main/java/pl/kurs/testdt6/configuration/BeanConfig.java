package pl.kurs.testdt6.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import pl.kurs.testdt6.exception.ExceptionModel;
import pl.kurs.testdt6.file.FileModel;

@Configuration
@EnableAsync
public class BeanConfig {

    @Bean
    FileModel fileModel() {
        return new FileModel();
    }

    @Bean
    ExceptionModel exceptionModel() {
        return new ExceptionModel();
    }
}
