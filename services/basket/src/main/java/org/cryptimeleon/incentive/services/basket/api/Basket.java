package org.cryptimeleon.incentive.services.basket.api;

import io.swagger.annotations.ApiModelProperty;
import org.cryptimeleon.incentive.services.basket.storage.BasketEntity;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Model class representing a basket.
 */
public class Basket {
    @ApiModelProperty("${basketModel.basketID}")
    private UUID basketID; // unique identifier for the basket
    @ApiModelProperty("${basketModel.basketItems}")
    private List<BasketItem> basketItems;
    /*
     * the list of reward items that the user gets once she redeems the basket
     */
    @ApiModelProperty("${basketModel.rewardItems}")
    private List<RewardItem> rewardItems;
    @ApiModelProperty("${basketModel.paid}")
    private boolean paid; // whether the basket has already been paid
    /*
     * Whether the user has already executed the spend and earn requests associated with this basket,
     * i.e. whether she has already received the points and rewards for it.
     */
    @ApiModelProperty("${basketModel.redeemed}")
    private boolean redeemed;
    /*
     * Baskets are locked before they are paid/redeemed
     * which means that no more items can be added to them.
     */
    @ApiModelProperty("${basketModel.locked}")
    private boolean locked;
    /*
     * The first redeem request that the user sent for this basket is stored.
     * This allows for the user to safely retry redeeming in case of connectivity problems
     * without allowing for easy double-spending attacks.
     */
    @ApiModelProperty("${basketModel.redeemRequest}")
    private String redeemRequest;
    /*
     * Total price of the basket,
     * i.e. sum of all item prices.
     */
    @ApiModelProperty("${basketModel.value}")
    private long value;

    @SuppressWarnings("unused")
    public Basket() {
    }

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

    public UUID getBasketID() {
        return this.basketID;
    }

    public List<BasketItem> getBasketItems() {
        return this.basketItems;
    }

    public List<RewardItem> getRewardItems() {
        return this.rewardItems;
    }

    public boolean isPaid() {
        return this.paid;
    }

    public boolean isRedeemed() {
        return this.redeemed;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public String getRedeemRequest() {
        return this.redeemRequest;
    }

    public long getValue() {
        return this.value;
    }

    public void setValue(final long value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Basket)) return false;
        final Basket other = (Basket) o;
        if (!other.canEqual(this)) return false;
        if (this.isPaid() != other.isPaid()) return false;
        if (this.isRedeemed() != other.isRedeemed()) return false;
        if (this.isLocked() != other.isLocked()) return false;
        if (this.getValue() != other.getValue()) return false;
        final Object this$basketID = this.getBasketID();
        final Object other$basketID = other.getBasketID();
        if (!Objects.equals(this$basketID, other$basketID)) return false;
        final Object this$basketItems = this.getBasketItems();
        final Object other$basketItems = other.getBasketItems();
        if (!Objects.equals(this$basketItems, other$basketItems))
            return false;
        final Object this$rewardItems = this.getRewardItems();
        final Object other$rewardItems = other.getRewardItems();
        if (!Objects.equals(this$rewardItems, other$rewardItems))
            return false;
        final Object this$redeemRequest = this.getRedeemRequest();
        final Object other$redeemRequest = other.getRedeemRequest();
        return Objects.equals(this$redeemRequest, other$redeemRequest);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Basket;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isPaid() ? 79 : 97);
        result = result * PRIME + (this.isRedeemed() ? 79 : 97);
        result = result * PRIME + (this.isLocked() ? 79 : 97);
        final long $value = this.getValue();
        result = result * PRIME + (int) ($value >>> 32 ^ $value);
        final Object $basketID = this.getBasketID();
        result = result * PRIME + ($basketID == null ? 43 : $basketID.hashCode());
        final Object $basketItems = this.getBasketItems();
        result = result * PRIME + ($basketItems == null ? 43 : $basketItems.hashCode());
        final Object $rewardItems = this.getRewardItems();
        result = result * PRIME + ($rewardItems == null ? 43 : $rewardItems.hashCode());
        final Object $redeemRequest = this.getRedeemRequest();
        result = result * PRIME + ($redeemRequest == null ? 43 : $redeemRequest.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Basket(basketID=" + this.getBasketID() + ", basketItems=" + this.getBasketItems() + ", rewardItems=" + this.getRewardItems() + ", paid=" + this.isPaid() + ", redeemed=" + this.isRedeemed() + ", locked=" + this.isLocked() + ", redeemRequest=" + this.getRedeemRequest() + ", value=" + this.getValue() + ")";
    }
}
