package org.cryptimeleon.incentive.services.bootstrap;

import org.cryptimeleon.incentive.client.dto.ItemDto;
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

    private static final ItemDto[] basketItems = {
            new ItemDto(
                    "4803216011183",
                    "Hazelnut Spread",
                    239),
            new ItemDto(
                    "6254500644798",
                    "Crunchy Hazelnut Spread",
                    269),
            new ItemDto(
                    "6834049853792",
                    "Green Tea",
                    289),
            new ItemDto(
                    "6881125218822",
                    "Chai Tea",
                    279),
            new ItemDto(
                    "9933606420495",
                    "Bottled Water",
                    35),
            new ItemDto(
                    "3383315467889",
                    "Shampoo",
                    459),
            new ItemDto(
                    "4698150688146",
                    "Chewing Gum",
                    99),
            new ItemDto(
                    "2678866375181",
                    "Cereal",
                    599),
            new ItemDto(
                    "5910439010530",
                    "Gummy Bears",
                    289),
    };

    private static final RewardItemDto[] rewardItems = {
            // Hazelnut Spread Promotion
            new RewardItemDto("160859564846", "Hazelnut Spread"),
            // VIP Promotion
            new RewardItemDto("957532923619", "VIP Bronze Bonus"), // Do not work yet (no effect), but for the sake of being a prototype let's call it discount
            new RewardItemDto("579999001166", "VIP Silver Bonus"),
            new RewardItemDto("188444480283", "VIP Gold Bonus"),
            // Streak Promotion
            new RewardItemDto("568948928121", "Coffee"),
            new RewardItemDto("132183798426", "Manicure Set"),
            new RewardItemDto("430370376573", "Knife Set")
    };

    private static final List<Promotion> promotions = new ArrayList<>(
            List.of(
                    new HazelPromotion(
                            HazelPromotion.generatePromotionParameters(),
                            "Hazelspread Promotion",
                            "Earn one point for every jar of hazelnut spread purchased!",
                            List.of(
                                    new HazelTokenUpdate(
                                            UUID.randomUUID(),
                                            "Get a free hazelnut spread for 4 points!",
                                            new RewardSideEffect("160859564846"),
                                            4
                                    )
                            ),
                            "hazelnut"
                    ),
                    new VipPromotion(
                            VipPromotion.generatePromotionParameters(),
                            "VIP Promotion",
                            "You can reach the VIP status Bronze, Silver and Gold by collecting points for every purchase.",
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
                                            "Increase your streak",
                                            new NoSideEffect(), // This is just the default update operation
                                            7
                                    ),
                                    new RangeProofStreakTokenUpdate(
                                            UUID.randomUUID(),
                                            "You get a free coffee if your streak is at least 5.",
                                            new RewardSideEffect("568948928121"),
                                            7,
                                            5
                                    ),
                                    new SpendStreakTokenUpdate(
                                            UUID.randomUUID(),
                                            "Get a free manicure set for 10 streak points.",
                                            new RewardSideEffect("132183798426"),
                                            7,
                                            10
                                    ),
                                    new SpendStreakTokenUpdate(
                                            UUID.randomUUID(),
                                            "Get a free knife set for 20 streak points.",
                                            new RewardSideEffect("430370376573"),
                                            7,
                                            20
                                    )
                            ),
                            7
                    )
            ));

    public static final BootstrapData bootstrapData = new BootstrapData(basketItems, rewardItems, promotions);
}