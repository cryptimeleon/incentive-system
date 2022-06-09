package org.cryptimeleon.incentive.promotion.model;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

/**
 * Data class that represents all items of a certain type that are in a basket.
 */
@Value
@AllArgsConstructor
public class BasketItem {
    String itemId;
    String title;
    int price;
    int count;

    /**
     * Old constructor. We do not use UUIDs for items anymore since they are too long for barcodes.
     */
    @Deprecated
    public BasketItem(UUID itemId, String title, int price, int count) {
        this.itemId = itemId.toString();
        this.title = title;
        this.price = price;
        this.count = count;
    }
}
