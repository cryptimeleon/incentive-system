package org.cryptimeleon.incentive.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A basket item represents an item that is in a basket and hence has a count field.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasketItemDto {
    String id;
    String title;
    int price;
    int count;
}
