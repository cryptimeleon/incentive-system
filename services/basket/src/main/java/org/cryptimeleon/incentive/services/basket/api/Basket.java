package org.cryptimeleon.incentive.services.basket.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cryptimeleon.incentive.services.basket.storage.BasketEntity;

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
    private UUID basketID; // unique identifier for the basket

    @ApiModelProperty(value = "${basketModel.basketItems}")
    private List<BasketItem> basketItems;
    /*
     * the list of reward items that the user gets once she redeems the basket
     */
    @ApiModelProperty(value = "${basketModel.rewardItems}")
    private List<RewardItem> rewardItems;

    @ApiModelProperty(value = "${basketModel.paid}")
    private boolean paid; // whether the basket has already been paid

    /*
     * Whether the user has already executed the spend and earn requests associated with this basket,
     * i.e. whether she has already received the points and rewards for it.
     */
    @ApiModelProperty(value = "${basketModel.redeemed}")
    private boolean redeemed;

    /*
     * Baskets are locked before they are paid/redeemed
     * which means that no more items can be added to them.
     */
    @ApiModelProperty(value = "${basketModel.locked}")
    private boolean locked;

    /*
     * The first redeem request that the user sent for this basket is stored.
     * This allows for the user to safely retry redeeming in case of connectivity problems
     * without allowing for easy double-spending attacks.
     */
    @ApiModelProperty(value = "${basketModel.redeemRequest}")
    private String redeemRequest;

    @ApiModelProperty(value = "${basketModel.value}")
    private long value;

    public Basket(BasketEntity basketEntity) {
        this.basketID = basketEntity.getBasketID();
        this.basketItems = basketEntity.getBasketItems().stream().map(e -> new BasketItem(e.getItem(), e.getCount())).collect(Collectors.toList());
        this.rewardItems = basketEntity.getRewardItems().stream().map(e -> new RewardItem(e.getId(), e.getTitle())).collect(Collectors.toList());
        this.paid = basketEntity.isPaid();
        this.redeemed = basketEntity.isRedeemed();
        this.locked = basketEntity.isLocked();
        this.redeemRequest = basketEntity.getRedeemRequest();
        this.value = basketEntity.getBasketItems().stream().mapToLong(i -> i.getCount() * i.getItem().getPrice()).sum();
    }
}
