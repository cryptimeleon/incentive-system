package org.cryptimeleon.incentive.promotion;

import lombok.Value;

import java.util.List;
import java.util.Map;

/**
 * Class that represents a basket.
 */
@Value
public class PromotionBasket {
    List<BasketItem> basketContent;
    Map<PromotionId, PromotionCredit> creditPerPromotion; // Number of points that this basket is worth and that were used

    /**
     * Compute value of a basket in cents.
     *
     * @return value of the basket
     */
    public long computeBasketValue() {
        return this.basketContent.stream()
                .mapToLong(basketItem -> basketItem.getCount() * basketItem.getItem().getPrice())
                .sum();
    }
}
