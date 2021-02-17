package de.upb.crypto.incentive.basketserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/** This class activates and configures SpringFox used for the Swagger UI. */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

  @Bean
  public Docket apiDocket() {
    return new Docket(DocumentationType.SWAGGER_2).groupName("basket-server-api").select().build();
  }
}
