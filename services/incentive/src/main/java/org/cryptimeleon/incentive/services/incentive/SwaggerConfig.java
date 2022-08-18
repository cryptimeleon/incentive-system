package org.cryptimeleon.incentive.services.incentive;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * This provides a swagger page for testing.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${springfox.host}")
    String host;

    @Bean
    public Docket apiDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .host(host)
                .select()
                .build();
    }
}

