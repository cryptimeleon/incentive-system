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

public class DefaultBootstrapData {

    private static final ItemDto[] basketItems = {
            new ItemDto(
                    "3941288190038",
                    "Sweetened Hazelnut Spread",
                    199),
            new ItemDto(
                    "1022525418053",
                    "Tomato",
                    29),
            new ItemDto(
                    "4621006331880",
                    "Apple",
                    59),
            new ItemDto(
                    "4536852654932",
                    "Peach",
                    99),
            new ItemDto(
                    "2936746557615",
                    "Potatoes",
                    299),
            new ItemDto(
                    "0680818152421",
                    "Mango",
                    119),
            new ItemDto(
                    "0280818152421",
                    "Peanut Butter",
                    519)
    };

    private static final RewardItemDto[] rewardItems = {
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

    public static BootstrapData bootstrapData = new BootstrapData(basketItems, rewardItems, promotions);
}
