package org.cryptimeleon.incentive.services.incentive;

import org.cryptimeleon.incentive.client.InfoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class IncentiveApplication {

    @Value("${info-service.url}")
    private String infoServiceUrl;

    public static void main(String[] args) {
        SpringApplication.run(IncentiveApplication.class, args);
    }

    @Bean
    InfoClient infoClient() {
        return new InfoClient(infoServiceUrl);
    }
}