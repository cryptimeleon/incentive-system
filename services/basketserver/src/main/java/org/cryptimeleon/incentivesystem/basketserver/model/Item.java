package org.cryptimeleon.incentivesystem.basketserver.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Dataclass representing items that be purchased in a basket.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @ApiModelProperty(value = "${item.id}")
    UUID id;
    @ApiModelProperty(value = "${item.title}")
    String title;
    @ApiModelProperty(value = "${item.price}")
    long price;
}
