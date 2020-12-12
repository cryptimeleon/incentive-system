package de.upb.crypto.incentive.services.issue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@SpringBootApplication
public class IssueApplication {

    public static void main(String[] args) {
        SpringApplication.run(IssueApplication.class, args);
    }

    // Configure Swagger API using springfox
    @Bean
    public Docket petApi() {
        System.out.println("docu");
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("issue-api")
                .select()
                .build()
                .pathMapping("/");
    }
}
