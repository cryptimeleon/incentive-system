package org.cryptimeleon.incentive.services.promotion;

import org.cryptimeleon.incentive.promotion.promotions.NutellaPromotion;
import org.cryptimeleon.incentive.promotion.promotions.Promotion;
import org.cryptimeleon.incentive.promotion.reward.NutellaReward;
import org.cryptimeleon.incentive.promotion.reward.RewardSideEffect;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PromotionRepository {
    private List<Promotion> promotions = List.of(
            new NutellaPromotion(
                    NutellaPromotion.generatePromotionParameters(),
                    List.of(
                            new NutellaReward(4, UUID.randomUUID(), new RewardSideEffect("Free Nutella")),
                            new NutellaReward(9, UUID.randomUUID(), new RewardSideEffect("Free Big nutella"))
                    )
            ),
            new NutellaPromotion(
                    NutellaPromotion.generatePromotionParameters(),
                    List.of(
                            new NutellaReward(9, UUID.randomUUID(), new RewardSideEffect("Free Teddy")),
                            new NutellaReward(6, UUID.randomUUID(), new RewardSideEffect("Free Pan"))
                    )
            )
    );

    public List<Promotion> getPromotions() {
        return promotions;
    }

    public Optional<Promotion> getPromotion(BigInteger promotionId) {
        return promotions.stream().filter(p -> p.getPromotionParameters().getPromotionId().equals(promotionId)).findAny();
    }
}
