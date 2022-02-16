package org.cryptimeleon.incentive.services.promotion.repository;

import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.streak.RangeProofStreakTokenUpdate;
import org.cryptimeleon.incentive.promotion.streak.SpendStreakTokenUpdate;
import org.cryptimeleon.incentive.promotion.streak.StandardStreakTokenUpdate;
import org.cryptimeleon.incentive.promotion.streak.StreakPromotion;
import org.cryptimeleon.incentive.promotion.vip.VipPromotion;
import org.cryptimeleon.incentive.services.promotion.IncentiveServiceException;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This repository manages all promotions.
 * Currently a hard-coded implementation. Will be replaced by a database in the future.
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
                                    new HazelTokenUpdate(UUID.randomUUID(), "Get a free Nutella for 4 points!", new RewardSideEffect("Free Nutella"), 4),
                                    new HazelTokenUpdate(UUID.randomUUID(), "Get a free Big Nutella for 9 points!", new RewardSideEffect("Free Big nutella"), 9)
                            ),
                            "nutella"
                    ),
                    new HazelPromotion(
                            HazelPromotion.generatePromotionParameters(),
                            "General Promotion",
                            "Earn one point for every item you buy!",
                            List.of(
                                    new HazelTokenUpdate(UUID.randomUUID(), "Get a free Teddy for 9 points!", new RewardSideEffect("Free Teddy"), 9),
                                    new HazelTokenUpdate(UUID.randomUUID(), "Get a free Pan for 6 points!", new RewardSideEffect("Free Pan"), 6)
                            ),
                            ""
                    ),
                    new VipPromotion(
                            VipPromotion.generatePromotionParameters(),
                            "VIP Promotion",
                            "You can reach the VIP status BRONZE, SILVER and Gold by collecting points for every purchase.",
                            100_00,
                            200_00,
                            500_00,
                            new RewardSideEffect("2% Discount"),
                            new RewardSideEffect("5% Discount"),
                            new RewardSideEffect("10% Discount")
                    ),
                    new StreakPromotion(
                            StreakPromotion.generatePromotionParameters(),
                            "Streak Promotion",
                            "Maintain a streak by shopping regularly. You lose your streak if you do not visit our store for 7 days in a row!",
                            List.of(
                                    new StandardStreakTokenUpdate(UUID.randomUUID(), "Increase or reset your streak", new RewardSideEffect(""), 7),
                                    new RangeProofStreakTokenUpdate(UUID.randomUUID(), "You get a free coffee if you're streak is at least 5.", new RewardSideEffect("Free Coffee"), 7, 5),
                                    new SpendStreakTokenUpdate(UUID.randomUUID(), "Get a free teddy in exchange for a streak of 10.", new RewardSideEffect("Teddy Bear"), 7, 10),
                                    new SpendStreakTokenUpdate(UUID.randomUUID(), "Get a free nonstick skillet exchange for a streak of 20.", new RewardSideEffect("Nonstick Skillet"), 7, 20)
                            ),
                            7
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
