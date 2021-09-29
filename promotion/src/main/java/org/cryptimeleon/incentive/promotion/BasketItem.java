package org.cryptimeleon.incentive.promotion;

import lombok.Value;

/**
 * Class that represents a single basket item.
 */
@Value
public class BasketItem {
    public String title;
    public String id;
    public long price;
}
