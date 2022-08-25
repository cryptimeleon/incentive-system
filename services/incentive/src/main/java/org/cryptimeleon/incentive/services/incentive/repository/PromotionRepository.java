package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.services.incentive.error.IncentiveServiceException;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This repository manages all promotions.
 * Currently a hard-coded implementation. Will be replaced by a database in the future.
 */
@Repository
public class PromotionRepository {
    private final List<Promotion> promotions = new ArrayList<>();

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

    public void deleteAllPromotions() {
        promotions.clear();
    }
}
