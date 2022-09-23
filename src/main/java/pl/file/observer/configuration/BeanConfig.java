package pl.file.observer.configuration;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import pl.file.observer.exception.ExceptionModel;
import pl.file.observer.job.JobStatusAdminModel;
import pl.file.observer.account.AccountEntity;
import pl.file.observer.job.JobEntity;
import pl.file.observer.job.JobStatusModel;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableAsync
@EnableSwagger2
public class BeanConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        configureJobStatusAdminModel(mapper);
        configureJobStatusModel(mapper);

        return mapper;
    }

    private void configureJobStatusModel(ModelMapper mapper) {
        PropertyMap<JobStatusModel, JobEntity> map = new PropertyMap<>() {
            @Override
            protected void configure() {

            }
        };
        mapper.addMappings(map);

        mapper.typeMap(JobEntity.class, JobStatusModel.class)
                .addMappings(x -> x.using(subscribeAmountConverter).map(JobEntity::getAccounts, JobStatusModel::setSubscribersAmount));
    }

    private void configureJobStatusAdminModel(ModelMapper mapper) {
        PropertyMap<JobStatusAdminModel, JobEntity> map = new PropertyMap<>() {
            @Override
            protected void configure() {
            }
        };
        mapper.addMappings(map);

        mapper.typeMap(JobEntity.class, JobStatusAdminModel.class)
                .addMappings(x -> x.using(subscribeAmountConverter).map(JobEntity::getAccounts, JobStatusAdminModel::setSubscribersAmount))
                .addMappings(x -> x.using(emailListConverter).map(JobEntity::getAccounts, JobStatusAdminModel::setEmailList));
    }

    private final Converter<Set<JobEntity>, Integer> subscribeAmountConverter = mappingContext -> {
        return Math.toIntExact(mappingContext.getSource().stream().map(JobEntity::getAccounts).count());
    };

    private final Converter<Set<AccountEntity>, Set<String>> emailListConverter = mappingContext -> {
        return mappingContext.getSource().stream()
                .map(AccountEntity::getEmail).collect(Collectors.toSet());
    };


    @Bean
    public ExceptionModel exceptionModel() {
        return new ExceptionModel();
    }

    @Bean
    public Docket swaggerConfiguration() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .select()
                .paths(PathSelectors.regex("/follow.*"))
                .apis(RequestHandlerSelectors.basePackage("pl.kurs.testdt6"))
                .build()
                .apiInfo(apiDetails())
                .securitySchemes(schemeList());
    }

    private List<? extends SecurityScheme> schemeList() {
        List<SecurityScheme> schemeList = new ArrayList<>();
        schemeList.add(new BasicAuth("basicAuth"));
        return schemeList;
    }

    private ApiInfo apiDetails() {
        return new ApiInfo(
                "File watch API",
                "API for watching file on server",
                "1.0",
                "Free",
                new springfox.documentation.service.Contact("test contact", "contact url", "contact@email.com"),
                "API License",
                "License url",
                Collections.emptyList()
        );
    }
}
