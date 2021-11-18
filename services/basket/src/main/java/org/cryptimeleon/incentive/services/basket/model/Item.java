package org.cryptimeleon.incentive.services.basket.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
