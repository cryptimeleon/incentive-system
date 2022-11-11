package org.cryptimeleon.incentive.services.basket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cryptimeleon.incentive.services.basket.storage.ItemEntity;

/**
 * Utility dataclass for representing Items in a Basket.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasketItem {
    ItemEntity item;
    int count;
}
