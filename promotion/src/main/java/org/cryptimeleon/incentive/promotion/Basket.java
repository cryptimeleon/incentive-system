package org.cryptimeleon.incentive.promotion;

import lombok.Value;

import java.util.List;

/**
 * Class that represents a basket.
 */
@Value
public class Basket {
    List<BasketItem> basketContent;

    /**
     * Compute value of a basket in cents.
     *
     * @return value of the basket
     */
    public long computeBasketValue() {
        return this.basketContent.stream().mapToLong(basketItem -> basketItem.getCount() * basketItem.getItem().getPrice()).sum();
    }
}
