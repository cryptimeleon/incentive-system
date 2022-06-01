package org.cryptimeleon.incentivesystem.services.bootstrap.bootstrap;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.dto.BasketItemDto;
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

    @Value("${basket-service.provider-secret}")
    private String basketServiceProviderSecret = "";

    private BasketItemDto[] defaultItems = {
            new BasketItemDto(
                    "3941288190038",
                    "Sweetened hazelnut cocoa spread",
                    199),
            new BasketItemDto(
                    "1022525418053",
                    "Tomato",
                    29),
            new BasketItemDto(
                    "4621006331880",
                    "Apple",
                    59),
            new BasketItemDto(
                    "4536852654932",
                    "Peach",
                    99),
            new BasketItemDto(
                    "2936746557615",
                    "Potatoes",
                    299),
            new BasketItemDto(
                    "0680818152421",
                    "Mango",
                    119),
            new BasketItemDto(
                    "0280818152421",
                    "Peanut Butter",
                    519)
    };

    public static void main(String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);
    }

    @Bean
    BasketClient basketClient() {
        return new BasketClient(basketServiceUrl);
    }

    @Bean
    public CommandLineRunner run(BasketClient basketClient) {
        return args -> {
            waitForBasketServiceOrThrow(basketClient);

            // Add basket items
            for (BasketItemDto item : defaultItems) {
                System.out.println("adding " + item);
                basketClient.newBasketItem(item, basketServiceProviderSecret).block();
            }
        };
    }

    @SneakyThrows
    private void waitForBasketServiceOrThrow(BasketClient basketClient) {
        int i = 1;
        while (i < 65) {
            try {
                basketClient.sendAliveRequest().block(Duration.ofSeconds(1));
                return;
            } catch (WebClientRequestException e) {
                log.info("Could not reach basket service. Retrying in " + i + " seconds");
                Thread.sleep(1000L * i);
            }
            i = i * 2;
        }
        throw new RuntimeException("Could not reach the basket service.");
    }
}