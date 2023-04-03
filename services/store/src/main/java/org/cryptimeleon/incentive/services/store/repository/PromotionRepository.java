package org.cryptimeleon.incentive.services.store.repository;

import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.services.store.StoreException;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PromotionRepository {
    private final List<Promotion> promotions = new ArrayList<>();

    public List<Promotion> getPromotions() {
        return promotions;
    }

    /**
     * Returns the promotion with the passed promotion ID if it exists.
     *
     * @return Optional
     */
    public Optional<Promotion> getPromotion(BigInteger promotionId) {
        return promotions.stream().filter(p -> p.getPromotionParameters().getPromotionId().equals(promotionId)).findAny();
    }

    /**
     * Adds the passed promotion to the system if its promotion ID is not used yet.
     *
     * @param promotion promotion object
     */
    public void addPromotion(Promotion promotion) {
        if (promotions.stream().noneMatch(p -> p.getPromotionParameters().getPromotionId().equals(promotion.getPromotionParameters().getPromotionId()))) {
            promotions.add(promotion);
        } else {
            throw new StoreException("PromotionId already used!");
        }
    }

    /**
     * Deletes all promotions from the system.
     */
    public void deleteAllPromotions() {
        promotions.clear();
    }
}
