package org.cryptimeleon.incentive.services.basket.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cryptimeleon.incentive.services.basket.storage.ItemEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasketItemNew {
    @ApiModelProperty(value = "${item.id}")
    String id;
    @ApiModelProperty(value = "${item.title}")
    String title;
    @ApiModelProperty(value = "${item.price}")
    long price;
    @ApiModelProperty(value = "${item.count}")
    int count;

    public BasketItemNew(ItemEntity itemEntity, int count) {
        this.id = itemEntity.getId();
        this.title = itemEntity.getTitle();
        this.price = itemEntity.getPrice();
        this.count = count;
    }

    public BasketItemNew(Item item, int count) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.price = item.getPrice();
        this.count = count;
    }
}
