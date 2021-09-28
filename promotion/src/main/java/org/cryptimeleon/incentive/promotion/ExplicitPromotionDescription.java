package org.cryptimeleon.incentive.promotion;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Define promotions by explicitly stating the amount of points a certain item is worth.
 */
public class ExplicitPromotionDescription extends PromotionDescription {

    public final List<PromotionReward> promotionRewards;
    public final Map<String, Long> pointsPerItemWithId;

    public ExplicitPromotionDescription(long promotionId,
                                        String promotionTitle,
                                        String promotionDescription,
                                        LocalDate promotionStart,
                                        LocalDate promotionEnd,
                                        List<PromotionReward> promotionRewards,
                                        Map<String, Long> pointsPerItemWithId) {
        super(promotionId, promotionTitle, promotionDescription, promotionStart, promotionEnd);
        this.promotionRewards = promotionRewards;
        this.pointsPerItemWithId = pointsPerItemWithId;
    }

    @Override
    public Long computePoints(Basket basket) {
        return basket.getBasketContent().entrySet().stream().
                mapToLong(basketItemLongEntry ->
                        basketItemLongEntry.getValue() * this.pointsPerItemWithId.getOrDefault(basketItemLongEntry.getKey().id, 0L)
                ).sum();
    }

    @Override
    public List<PromotionReward> qualifiedRewards(long points) {
        return promotionRewards.stream().filter(promotionReward -> promotionReward.getPrice() <= points).collect(Collectors.toList());
    }
}
