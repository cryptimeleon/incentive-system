package org.cryptimeleon.incentive.services.bootstrap;

import org.cryptimeleon.incentive.client.AliveEndpoint;
import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.IncentiveClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
    private final BootstrapDataChoice BOOTSTRAP_DATA_CHOICE = BootstrapDataChoice.DEMO;

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
            if (promotionServiceProviderSecret.equals("")) {
                log.error("promotionServiceProviderSecret is empty!");
                throw new RuntimeException();
            }
            log.info("Promotion service provider secret: {}", promotionServiceProviderSecret);
            waitForIncentiveServiceOrThrow(incentiveClient);
            BootstrapProviderClient bootstrapClient = new BootstrapProviderClient(promotionServiceProviderSecret, incentiveClient);
            bootstrapClient.publishBootstrapData(BOOTSTRAP_DATA_CHOICE);

            for (int i = 0; i < basketServiceUrls.length; i++) {
                String basketServiceProviderSecret = basketServiceProviderSecrets[i];
                if (basketServiceProviderSecret.equals("")) {
                    log.error("basketServiceProviderSecret #{} is empty!", i);
                    throw new RuntimeException();
                }
                log.info("Basket service provider secret #{}: {}", i, basketServiceProviderSecret);
                BasketClient basketClient = new BasketClient(basketServiceUrls[i]);
                waitForBasketServiceOrThrow(basketClient);
                BootstrapBasketClient bootstrapBasketClient = new BootstrapBasketClient(basketServiceProviderSecret, basketClient);
                bootstrapBasketClient.publishBootstrapData(BOOTSTRAP_DATA_CHOICE);
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
        int i = 0;
        while (i < 240) {
            try {
                aliveEndpoint.sendAliveRequest().block(Duration.ofSeconds(5));
                return;
            } catch (Exception e) {
                log.info("Failed to reach " + serviceName + "service.");
                Thread.sleep(1000L);
            }
            i++;
        }
        throw new RuntimeException("Could not reach the " + serviceName + "service.");
    }
}
