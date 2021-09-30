package org.cryptimeleon.incentive.promotion;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

/**
 * Logic of promotions where users earn a fixed amount of points per cent spent on any product.
 * Can be leveraged by changing the factor of points earned per cent spent.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class ProportionalPromotionDescription extends PromotionDescription {

    int pointsPerCent;

    public ProportionalPromotionDescription(long promotionId,
                                            String promotionTitle,
                                            String promotionDescription,
                                            LocalDate promotionStart,
                                            LocalDate promotionEnd,
                                            int pointsPerCent,
                                            List<PromotionReward> promotionRewards) {
        super(promotionId, promotionTitle, promotionDescription, promotionStart, promotionEnd, promotionRewards);
        this.pointsPerCent = pointsPerCent;
    }

    @Override
    public Long computePoints(Basket basket) {
        return basket.computeBasketValue() * pointsPerCent;
    }
}
