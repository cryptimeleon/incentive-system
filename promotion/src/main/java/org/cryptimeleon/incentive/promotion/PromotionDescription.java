package org.cryptimeleon.incentive.promotion;


import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * Abstract class that represents a definition of a promotion.
 * Is able to compute points that can be earned from a basket for that promotion, rewards that a tokens count
 * qualifies to and a hybrid of both, where basket contents are taken into account to reduce the points required to
 * claim a reward and then excluded from the points the user can earn afterwards.
 */
@Getter
abstract public class PromotionDescription {
    PromotionId promotionId;
    String promotionTitle;
    String promotionDescription;
    LocalDate promotionStart;
    LocalDate promotionEnd;

    public PromotionDescription(PromotionId promotionId,
                                String promotionTitle,
                                String promotionDescription,
                                LocalDate promotionStart,
                                LocalDate promotionEnd,
                                List<PromotionReward> promotionRewards) {
        this.promotionId = promotionId;
        this.promotionTitle = promotionTitle;
        this.promotionDescription = promotionDescription;
        this.promotionStart = promotionStart;
        this.promotionEnd = promotionEnd;
    }

    /**
     * Verify that this promotion is valid on the given date.
     */
    public boolean isValidAt(LocalDate date) {
        return !date.isBefore(promotionStart) && !date.isAfter(promotionEnd);
    }

    /**
     * Computes the points that are can be earned for this promotion from a basket.
     */
    public abstract Long computePoints(PromotionBasket promotionBasket);

    /**
     * Return a list of all rewards.
     */
    public abstract List<PromotionRewardDescription> getPromotionRewards();
}
