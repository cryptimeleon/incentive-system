package org.cryptimeleon.incentive.services.basket.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cryptimeleon.incentive.services.basket.storage.BasketEntity;
import org.cryptimeleon.incentive.services.basket.storage.ItemInBasketEntity;

import java.util.*;
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
    @ApiModelProperty(value = "${basketModel.items}")
    private Map<String, Integer> items;
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

    public Basket(UUID id) {
        basketID = id;
        items = new HashMap<>();
        rewardItems = new ArrayList<>();
        paid = false;
        locked = false;
        redeemed = false;
        redeemRequest = "";
    }

    public Basket(BasketEntity basketEntity) {
        this.basketID = basketEntity.getBasketID();
        this.items = basketEntity.getBasketItems().stream().collect(
                Collectors.toMap(
                        (ItemInBasketEntity i) -> i.getId().getItemId(),
                        ItemInBasketEntity::getCount
            )
        );
        this.rewardItems = new ArrayList<>(basketEntity.getRewardItems());
        this.paid = basketEntity.isPaid();
        this.redeemed = basketEntity.isRedeemed();
        this.locked = basketEntity.isLocked();
        this.redeemRequest = basketEntity.getRedeemRequest();
        this.value = basketEntity.getBasketItems().stream().mapToLong(i -> i.getCount() * i.getItem().getPrice()).sum();
    }
}
