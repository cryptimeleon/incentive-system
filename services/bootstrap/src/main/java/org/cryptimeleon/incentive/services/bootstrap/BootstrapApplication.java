package org.cryptimeleon.incentive.services.bootstrap;

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

@SpringBootApplication
public class BootstrapApplication {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BootstrapApplication.class);
    @Value("${basket-service.urls}")
    private String[] basketServiceUrls;
    @Value("${incentive-service.url}")
    private String incentiveServiceUrl = "";
    @Value("${basket-service.provider-secrets}")
    private String[] basketServiceProviderSecrets;
    @Value("${incentive-service.provider-secret}")
    private String promotionServiceProviderSecret = "";

    public static void main(String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);
    }

    @Bean
    IncentiveClient incentiveClient() {
        return new IncentiveClient(incentiveServiceUrl);
    }

    @Bean
    public CommandLineRunner run(IncentiveClient incentiveClient) {
        return args -> {
            for (int i = 0; i < basketServiceUrls.length; i++) {
                String basketServiceProviderSecret = basketServiceProviderSecrets[i];
                if (basketServiceProviderSecret.equals("")) {
                    log.error("basketServiceProviderSecret #{} is empty!", i);
                    throw new RuntimeException();
                }
                if (promotionServiceProviderSecret.equals("")) {
                    log.error("promotionServiceProviderSecret is empty!");
                    throw new RuntimeException();
                }
                log.info("Basket service provider secret: {}", basketServiceProviderSecret);
                log.info("Promotion service provider secret: {}", promotionServiceProviderSecret);
                BasketClient basketClient = new BasketClient(basketServiceUrls[i]);
                waitForBasketServiceOrThrow(basketClient);
                waitForIncentiveServiceOrThrow(incentiveClient);
                BootstrapClient bootstrapClient = new BootstrapClient(basketServiceProviderSecret, promotionServiceProviderSecret, basketClient, incentiveClient);
                bootstrapClient.publishBootstrapData(BootstrapDataChoice.DEMO);
            }
        };
    }

    private void waitForBasketServiceOrThrow(BasketClient basketClient) throws InterruptedException {
        waitForServiceOrThrow(basketClient, "basket");
    }

    private void waitForIncentiveServiceOrThrow(IncentiveClient incentiveClient) throws InterruptedException {
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
