package org.cryptimeleon.incentive.promotion;

import lombok.Value;

/**
 * Data class for items with their count.
 */
@Value
public class BasketItem {
    Item item;
    int count;
}
