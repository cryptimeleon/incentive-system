package org.cryptimeleon.incentive.promotion;

import lombok.Value;

/**
 * Class that represents a single basket item.
 */
@Value
public class Item {
    String title;
    String id;
    long price;
}
