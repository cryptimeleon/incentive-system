package org.cryptimeleon.incentive.promotion;


import java.time.LocalDate;
import java.util.List;

/**
 * Abstract class that represents a definition of a promotion.
 */
abstract public class PromotionDescription {
    public final long promotionId;
    public final String promotionTitle;
    public final String promotionDescription;
    public final LocalDate promotionStart;
    public final LocalDate promotionEnd;

    public PromotionDescription(long promotionId, String promotionTitle, String promotionDescription, LocalDate promotionStart, LocalDate promotionEnd) {
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
     * Computes the points that are can be earned for this promotion.
     */
    public abstract Long computePoints(Basket basket);

    /**
     * Return a list of all rewards for which the given token points count qualifies one.
     */
    public abstract List<PromotionReward> qualifiedRewards(long points);
}
