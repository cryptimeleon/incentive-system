package org.cryptimeleon.incentive.services.basket.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cryptimeleon.incentive.services.basket.storage.ItemEntity;

/**
 * Dataclass representing items that be purchased in a basket.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @ApiModelProperty(value = "${item.id}")
    String id;
    @ApiModelProperty(value = "${item.title}")
    String title;
    @ApiModelProperty(value = "${item.price}")
    long price;

    public Item(ItemEntity itemEntity) {
        this.id = itemEntity.getId();
        this.title = itemEntity.getTitle();
        this.price = itemEntity.getPrice();
    }
}
