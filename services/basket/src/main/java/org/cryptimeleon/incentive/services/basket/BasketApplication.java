package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.client.IncentiveClient;
import org.cryptimeleon.incentive.client.InfoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BasketApplication {

    @Value("${info-service.url}")
    private String infoServiceUrl;

    @Value("${incentive-service.url}")
    private String incentiveServiceUrl;

    public static void main(String[] args) {
        SpringApplication.run(BasketApplication.class, args);
    }

    @Bean
    InfoClient infoClient() {
        return new InfoClient(infoServiceUrl);
    }

    @Bean
    IncentiveClient incentiveClient() {
        return new IncentiveClient(incentiveServiceUrl);
    }
}
