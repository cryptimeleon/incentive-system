package org.cryptimeleon.incentive.services.promotion.repository;

import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.incentive.services.promotion.IncentiveServiceException;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This repository manages all promotions.
 */
@Repository
public class PromotionRepository {
    private final List<Promotion> promotions = new ArrayList<>(
            List.of(
                    new HazelPromotion(
                            HazelPromotion.generatePromotionParameters(),
                            "Nutella Promotion",
                            "Earn one point for every jar of Nutella purchased!",
                            List.of(
                                    new HazelTokenUpdate(4, "Get a free Nutella for 4 points!", UUID.randomUUID(), new RewardSideEffect("Free Nutella")),
                                    new HazelTokenUpdate(9, "Get a free Big Nutella for 9 points!", UUID.randomUUID(), new RewardSideEffect("Free Big nutella"))
                            ),
                            "nutella"
                    ),
                    new HazelPromotion(
                            HazelPromotion.generatePromotionParameters(),
                            "General Promotion",
                            "Earn one point item purchased!",
                            List.of(
                                    new HazelTokenUpdate(9, "Get a free Teddy for 9 points!", UUID.randomUUID(), new RewardSideEffect("Free Teddy")),
                                    new HazelTokenUpdate(6, "Get a free Pan for 6 points!", UUID.randomUUID(), new RewardSideEffect("Free Pan"))
                            ),
                            ""
                    )
            ));

    public List<Promotion> getPromotions() {
        return promotions;
    }

    public Optional<Promotion> getPromotion(BigInteger promotionId) {
        return promotions.stream().filter(p -> p.getPromotionParameters().getPromotionId().equals(promotionId)).findAny();
    }

    public void addPromotion(Promotion promotion) {
        if (promotions.stream().noneMatch(p -> p.getPromotionParameters().getPromotionId().equals(promotion.getPromotionParameters().getPromotionId()))) {
            promotions.add(promotion);
        } else {
            throw new IncentiveServiceException("PromotionId already used!");
        }
    }
}
