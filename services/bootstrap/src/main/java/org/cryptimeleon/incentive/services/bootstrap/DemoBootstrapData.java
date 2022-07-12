package org.cryptimeleon.incentive.services.bootstrap;

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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DemoBootstrapData {

    private static final BasketItemDto[] basketItems = {
            new BasketItemDto(
                    "4008400404127",
                    "Nutella",
                    239),
            new BasketItemDto(
                    "4001257000122",
                    "Meßmer Grüner Tee Vanille",
                    289),
            new BasketItemDto(
                    "8718951312432",
                    "Colgate Zahnpasta",
                    199),
    };

    private static final RewardItemDto[] rewardItems = {
            // Hazelnut Spread Promotion
            new RewardItemDto("160859564846", "Nutella"),
            // VIP Promotion
            new RewardItemDto("957532923619", "2% Discount"), // Do not work yet (no effect), but for the sake of being a prototype let's call it discount
            new RewardItemDto("579999001166", "5% Discount"),
            new RewardItemDto("188444480283", "10% Discount"),
            // Streak Promotion
            new RewardItemDto("568948928121", "Coffee"),
            new RewardItemDto("132183798426", "Manicure Set"),
            new RewardItemDto("430370376573", "Knife Set")
    };

    private static final List<Promotion> promotions = new ArrayList<>(
            List.of(
                    new HazelPromotion(
                            HazelPromotion.generatePromotionParameters(),
                            "Nutella Promotion",
                            "Earn one point for every jar of Nutella purchased!",
                            List.of(
                                    new HazelTokenUpdate(
                                            UUID.randomUUID(),
                                            "Get a free Nutella for 4 points!",
                                            new RewardSideEffect("160859564846"),
                                            4
                                    )
                            ),
                            "Nutella"
                    ),
                    new VipPromotion(
                            VipPromotion.generatePromotionParameters(),
                            "VIP Promotion",
                            "You can reach the VIP status BRONZE, SILVER and Gold by collecting points for every purchase.",
                            100_00, // costs are in cent, hence the _
                            200_00,
                            500_00,
                            new RewardSideEffect("957532923619"),
                            new RewardSideEffect("579999001166"),
                            new RewardSideEffect("188444480283")
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
                                            new RewardSideEffect("568948928121"),
                                            7,
                                            5
                                    ),
                                    new SpendStreakTokenUpdate(
                                            UUID.randomUUID(),
                                            "Get a free manicure set in exchange for a streak of 10.",
                                            new RewardSideEffect("132183798426"),
                                            7,
                                            10
                                    ),
                                    new SpendStreakTokenUpdate(
                                            UUID.randomUUID(),
                                            "Get a free knife set in exchange for a streak of 20.",
                                            new RewardSideEffect("430370376573"),
                                            7,
                                            20
                                    )
                            ),
                            7
                    )
            ));

    public static BootstrapData bootstrapData = new BootstrapData(basketItems, rewardItems, promotions);
}
