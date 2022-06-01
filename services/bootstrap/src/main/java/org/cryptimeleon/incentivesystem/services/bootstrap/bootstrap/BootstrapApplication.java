package org.cryptimeleon.incentivesystem.services.bootstrap.bootstrap;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.dto.BasketItemDto;
import org.cryptimeleon.incentive.client.dto.RewardItemDto;
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


    private RewardItemDto[] rewardItems = {
            // Hazelnut Spread Promotion
            new RewardItemDto("0580082614202", "Hazelnut Spread"),
            new RewardItemDto("4499722672546", "Large Hazelnut Spread"),
            // General Promotion
            new RewardItemDto("4460463579054", "Teddy Bear"),
            new RewardItemDto("0916751964193", "Pan"),
            // VIP Promotion
            new RewardItemDto("8445463753160", "2% Discount"), // Do not work yet (no effect), but for the sake of being a prototype let's call it discount
            new RewardItemDto("0789590748887", "5% Discount"),
            new RewardItemDto("1393421332370", "10% Discount"),
            // Streak Promotion
            new RewardItemDto("2413860782644", "Coffee"),
            new RewardItemDto("0750769787791", "Manicure Set"),
            new RewardItemDto("0182420525002", "Knife Set")
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

            for (BasketItemDto item : defaultItems) {
                System.out.println("adding " + item);
                basketClient.newBasketItem(item, basketServiceProviderSecret).block();
            }

            for (RewardItemDto rewardItem : rewardItems) {
                System.out.println("adding " + rewardItem);
                basketClient.newRewardItem(rewardItem, basketServiceProviderSecret).block();
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