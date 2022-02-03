package pl.kurs.testdt6.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import pl.kurs.testdt6.exception.ExceptionModel;
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

@Configuration
@EnableAsync
@EnableSwagger2
public class BeanConfig {


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
                .securitySchemes(sechemeList());
    }

    private List<? extends SecurityScheme> sechemeList() {
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
