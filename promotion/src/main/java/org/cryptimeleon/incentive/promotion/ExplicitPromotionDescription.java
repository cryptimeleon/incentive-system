package org.cryptimeleon.incentive.promotion;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Define promotions by explicitly stating the amount of points a certain item is worth.
 */
public class ExplicitPromotionDescription extends PromotionDescription {

    public final Map<String, Long> pointsPerItemWithId;

    public ExplicitPromotionDescription(long promotionId,
                                        String promotionTitle,
                                        String promotionDescription,
                                        LocalDate promotionStart,
                                        LocalDate promotionEnd,
                                        List<PromotionReward> promotionRewards,
                                        Map<String, Long> pointsPerItemWithId) {
        super(promotionId, promotionTitle, promotionDescription, promotionStart, promotionEnd, promotionRewards);
        this.pointsPerItemWithId = pointsPerItemWithId;
    }

    @Override
    public Long computePoints(Basket basket) {
        return basket.getBasketContent().stream().
                mapToLong(basketItem ->
                        basketItem.getCount() * this.pointsPerItemWithId.getOrDefault(basketItem.getItem().getId(), 0L)
                ).sum();
    }
}
