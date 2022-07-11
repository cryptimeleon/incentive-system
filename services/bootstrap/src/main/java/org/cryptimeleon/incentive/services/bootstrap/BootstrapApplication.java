package org.cryptimeleon.incentive.services.bootstrap;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.AliveEndpoint;
import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.IncentiveClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.Duration;

@Slf4j
@SpringBootApplication
public class BootstrapApplication {

    @Value("${basket-service.url}")
    private String basketServiceUrl = "";

    @Value("${incentive-service.url}")
    private String incentiveServiceUrl = "";

    @Value("${basket-service.provider-secret}")
    private String basketServiceProviderSecret = "";

    @Value("${incentive-service.provider-secret}")
    private String promotionServiceProviderSecret = "";


    public static void main(String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);
    }

    @Bean
    BasketClient basketClient() {
        return new BasketClient(basketServiceUrl);
    }

    @Bean
    IncentiveClient incentiveClient() {
        return new IncentiveClient(incentiveServiceUrl);
    }

    @Bean
    public CommandLineRunner run(BasketClient basketClient, IncentiveClient incentiveClient) {
        return args -> {
            if (basketServiceProviderSecret.equals("")) {
                log.error("basketServiceProviderSecret is empty!");
                throw new RuntimeException();
            }

            if (promotionServiceProviderSecret.equals("")) {
                log.error("promotionServiceProviderSecret is empty!");
                throw new RuntimeException();
            }

            log.info("Basket service provider secret: {}", basketServiceProviderSecret);
            log.info("Promotion service provider secret: {}", promotionServiceProviderSecret);

            waitForBasketServiceOrThrow(basketClient);
            waitForIncentiveServiceOrThrow(incentiveClient);

            BootstrapClient bootstrapClient = new BootstrapClient(basketServiceProviderSecret, promotionServiceProviderSecret, basketClient, incentiveClient);
            bootstrapClient.publishBootstrapData(DefaultBootstrapData.bootstrapData);
        };
    }

    @SneakyThrows
    private void waitForBasketServiceOrThrow(BasketClient basketClient) {
        waitForServiceOrThrow(basketClient, "basket");
    }

    @SneakyThrows
    private void waitForIncentiveServiceOrThrow(IncentiveClient incentiveClient) {
        waitForServiceOrThrow(incentiveClient, "incentive");
    }

    private void waitForServiceOrThrow(AliveEndpoint aliveEndpoint, String serviceName) throws InterruptedException {
        int i = 1;
        while (i < 65) {
            try {
                aliveEndpoint.sendAliveRequest().block(Duration.ofSeconds(1));
                return;
            } catch (WebClientRequestException e) {
                log.info("Could not reach " + serviceName + "service. Retrying in " + i + " seconds");
                Thread.sleep(1000L * i);
            }
            i = i * 2;
        }
        throw new RuntimeException("Could not reach the " + serviceName + "service.");
    }
}