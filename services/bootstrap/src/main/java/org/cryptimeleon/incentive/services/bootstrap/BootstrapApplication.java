package org.cryptimeleon.incentive.services.bootstrap;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.AliveEndpoint;
import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.IncentiveClient;
import org.cryptimeleon.incentive.client.dto.BasketItemDto;
import org.cryptimeleon.incentive.client.dto.RewardItemDto;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.sideeffect.NoSideEffect;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.streak.RangeProofStreakTokenUpdate;
import org.cryptimeleon.incentive.promotion.streak.SpendStreakTokenUpdate;
import org.cryptimeleon.incentive.promotion.streak.StandardStreakTokenUpdate;
import org.cryptimeleon.incentive.promotion.streak.StreakPromotion;
import org.cryptimeleon.incentive.promotion.vip.VipPromotion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private List<Promotion> promotions = new ArrayList<>(
            List.of(
                    new HazelPromotion(
                            HazelPromotion.generatePromotionParameters(),
                            "Nutella Promotion",
                            "Earn one point for every jar of Nutella purchased!",
                            List.of(
                                    new HazelTokenUpdate(
                                            UUID.randomUUID(),
                                            "Get a free hazelnut spread for 4 points!",
                                            new RewardSideEffect("0580082614202"),
                                            4
                                    ),
                                    new HazelTokenUpdate(
                                            UUID.randomUUID(),
                                            "Get a free large hazelnut spread for 9 points!",
                                            new RewardSideEffect("4499722672546"),
                                            9
                                    )
                            ),
                            "hazel"
                    ),
                    new HazelPromotion(
                            HazelPromotion.generatePromotionParameters(),
                            "General Promotion",
                            "Earn one point for every item you buy!",
                            List.of(
                                    new HazelTokenUpdate(
                                            UUID.randomUUID(),
                                            "Get a free Teddy for 500 points!",
                                            new RewardSideEffect("4460463579054"),
                                            500),
                                    new HazelTokenUpdate(
                                            UUID.randomUUID(),
                                            "Get a free Pan for 1000 points!",
                                            new RewardSideEffect("0916751964193"),
                                            1000)
                            ),
                            ""
                    ),
                    new VipPromotion(
                            VipPromotion.generatePromotionParameters(),
                            "VIP Promotion",
                            "You can reach the VIP status BRONZE, SILVER and Gold by collecting points for every purchase.",
                            100_00, // costs are in cent, hence the _
                            200_00,
                            500_00,
                            new RewardSideEffect("8445463753160"),
                            new RewardSideEffect("0789590748887"),
                            new RewardSideEffect("1393421332370")
                    ),
                    new StreakPromotion(
                            StreakPromotion.generatePromotionParameters(),
                            "Streak Promotion",
                            "Maintain a streak by shopping regularly. You lose your streak if you do not visit our store for 7 days in a row!",
                            List.of(
                                    new StandardStreakTokenUpdate(
                                            UUID.randomUUID(),
                                            "Increase or reset your streak",
                                            new NoSideEffect(), // This is just the default update operation
                                            7
                                    ),
                                    new RangeProofStreakTokenUpdate(
                                            UUID.randomUUID(),
                                            "You get a free coffee if you're streak is at least 5.",
                                            new RewardSideEffect("2413860782644"),
                                            7,
                                            5
                                    ),
                                    new SpendStreakTokenUpdate(
                                            UUID.randomUUID(),
                                            "Get a free manicure set in exchange for a streak of 10.",
                                            new RewardSideEffect("0750769787791"),
                                            7,
                                            10
                                    ),
                                    new SpendStreakTokenUpdate(
                                            UUID.randomUUID(),
                                            "Get a free knife set in exchange for a streak of 20.",
                                            new RewardSideEffect("0182420525002"),
                                            7,
                                            20
                                    )
                            ),
                            7
                    )
            ));

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
            for (BasketItemDto item : defaultItems) {
                log.info("adding " + item);
                basketClient.newBasketItem(item, basketServiceProviderSecret).block();
            }

            for (RewardItemDto rewardItem : rewardItems) {
                log.info("adding " + rewardItem);
                basketClient.newRewardItem(rewardItem, basketServiceProviderSecret).block();
            }

            waitForIncentiveServiceOrThrow(incentiveClient);
            incentiveClient.addPromotions(promotions, promotionServiceProviderSecret).block();
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