package org.cryptimeleon.incentive.services.credit;

import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.InfoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CreditApplication {

    @Value("${info-service.url}")
    private String infoServiceUrl;

    @Value("${basket-service.url}")
    private String basketUrl;

    public static void main(String[] args) {
        SpringApplication.run(CreditApplication.class, args);
    }

    @Bean
    InfoClient infoClient() {
        return new InfoClient(infoServiceUrl);
    }

    @Bean
    BasketClient basketClient() {
        return new BasketClient(basketUrl);
    }
}
