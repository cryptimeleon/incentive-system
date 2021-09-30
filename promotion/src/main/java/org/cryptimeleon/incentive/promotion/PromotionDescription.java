package org.cryptimeleon.incentive.promotion;


import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract class that represents a definition of a promotion.
 */
@Getter
abstract public class PromotionDescription {
    long promotionId;
    String promotionTitle;
    String promotionDescription;
    LocalDate promotionStart;
    LocalDate promotionEnd;
    List<PromotionReward> promotionRewards;

    public PromotionDescription(long promotionId,
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
        this.promotionRewards = promotionRewards;
    }

    /**
     * Verify that this promotion is valid on the given date.
     */
    public boolean isValidAt(LocalDate date) {
        return !date.isBefore(promotionStart) && !date.isAfter(promotionEnd);
    }

    /**
     * Computes the points that are can be earned for this promotion.
     */
    public abstract Long computePoints(Basket basket);

    /**
     * Return a list of all rewards for which the given token points count qualifies one.
     */
    public List<PromotionReward> qualifiedRewards(long points) {
        return promotionRewards.stream().filter(promotionReward -> promotionReward.getPrice() <= points).collect(Collectors.toList());
    }
}
