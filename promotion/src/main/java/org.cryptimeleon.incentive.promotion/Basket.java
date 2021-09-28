package org.cryptimeleon.incentive.promotion;

import lombok.Value;

import java.util.Map;

/**
 * Class that represents a basket.
 */
@Value
public class Basket {
    Map<BasketItem, Long> basketContent;
}
