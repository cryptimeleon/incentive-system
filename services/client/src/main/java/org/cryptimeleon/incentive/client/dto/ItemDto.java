package org.cryptimeleon.incentive.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An item represents an item that can be added to a basket.
 * Hence, it does not have a count field as in the BasketItemDto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    String id;
    String title;
    int price;
}
