package org.cryptimeleon.incentive.services.promotion.repository;

import org.cryptimeleon.incentive.promotion.promotions.NutellaPromotion;
import org.cryptimeleon.incentive.promotion.promotions.Promotion;
import org.cryptimeleon.incentive.promotion.reward.NutellaReward;
import org.cryptimeleon.incentive.promotion.reward.RewardSideEffect;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This repository manages all promotions.
 */
@Repository
public class PromotionRepository {
    private final List<Promotion> promotions = List.of(
            new NutellaPromotion(
                    NutellaPromotion.generatePromotionParameters(),
                    "Nutella Promotion",
                    "Earn one point for every jar of Nutella purchased!",
                    List.of(
                            new NutellaReward(4, "Get a free Nutella for 4 points!", UUID.randomUUID(), new RewardSideEffect("Free Nutella")),
                            new NutellaReward(9, "Get a free Big Nutella for 9 points!", UUID.randomUUID(), new RewardSideEffect("Free Big nutella"))
                    ),
                    "nutella"
            ),
            new NutellaPromotion(
                    NutellaPromotion.generatePromotionParameters(),
                    "General Promotion",
                    "Earn one point item purchased!",
                    List.of(
                            new NutellaReward(9, "Get a free Teddy for 9 points!", UUID.randomUUID(), new RewardSideEffect("Free Teddy")),
                            new NutellaReward(6, "Get a free Pan for 6 points!", UUID.randomUUID(), new RewardSideEffect("Free Pan"))
                    ),
                    ""
            )
    );

    public List<Promotion> getPromotions() {
        return promotions;
    }

    public Optional<Promotion> getPromotion(BigInteger promotionId) {
        return promotions.stream().filter(p -> p.getPromotionParameters().getPromotionId().equals(promotionId)).findAny();
    }
}
