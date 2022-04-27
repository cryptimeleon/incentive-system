package org.cryptimeleon.incentive.services.basket.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dataclass representing dumb coupons / reward items added by the incentive service after successful spend-deduct.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RewardItem {
    @ApiModelProperty(value = "${item.id}")
    String id;
    @ApiModelProperty(value = "${item.title}")
    String title;
}
