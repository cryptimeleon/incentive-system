package org.cryptimeleon.incentive.promotion.model;

import lombok.Value;

import java.util.UUID;

@Value
public class BasketItem {
    UUID itemId;
    String title;
    int price;
    int count;
}
