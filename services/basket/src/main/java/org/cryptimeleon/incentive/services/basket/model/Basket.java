package org.cryptimeleon.incentive.services.basket.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cryptimeleon.incentive.services.basket.storage.BasketEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Model class representing a basket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Basket {
    @ApiModelProperty(value = "${basketModel.basketID}")
    private UUID basketID;
    @ApiModelProperty(value = "${basketModel.basketItems}")
    private List<BasketItem> basketItems;
    @ApiModelProperty(value = "${basketModel.rewardItems}")
    private List<String> rewardItems;
    @ApiModelProperty(value = "${basketModel.paid}")
    private boolean paid;
    @ApiModelProperty(value = "${basketModel.redeemed}")
    private boolean redeemed;
    @ApiModelProperty(value = "${basketModel.locked}")
    private boolean locked;
    @ApiModelProperty(value = "${basketModel.redeemRequest}")
    private String redeemRequest;
    // value must be set manually for serialization
    @ApiModelProperty(value = "${basketModel.value}")
    private long value;

    public Basket(BasketEntity basketEntity) {
        this.basketID = basketEntity.getBasketID();
        this.basketItems = basketEntity.getBasketItems().stream().map(e ->
                new BasketItem(e.getItem(), e.getCount())
        ).collect(Collectors.toList());
        this.rewardItems = new ArrayList<>(basketEntity.getRewardItems());
        this.paid = basketEntity.isPaid();
        this.redeemed = basketEntity.isRedeemed();
        this.locked = basketEntity.isLocked();
        this.redeemRequest = basketEntity.getRedeemRequest();
        this.value = basketEntity.getBasketItems().stream().mapToLong(i -> i.getCount() * i.getItem().getPrice()).sum();
    }
}
