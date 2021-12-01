package org.cryptimeleon.incentive.promotion.model;

import lombok.Value;

import java.util.UUID;

/**
 * Data class that represents all items of a certain type that are in a basket.
 */
@Value
public class BasketItem {
    UUID itemId;
    String title;
    int price;
    int count;
}
