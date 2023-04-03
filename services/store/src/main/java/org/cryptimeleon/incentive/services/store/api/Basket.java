package org.cryptimeleon.incentive.services.store.api;

import io.swagger.annotations.ApiModelProperty;
import org.cryptimeleon.incentive.services.store.storage.BasketEntity;

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
     * Baskets are locked before they are paid/redeemed
     * which means that no more items can be added to them.
     */
    @ApiModelProperty("${basketModel.locked}")
    private boolean locked;
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
        this.locked = basketEntity.isLocked();
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

    public boolean isLocked() {
        return this.locked;
    }

    public long getValue() {
        return this.value;
    }

    public void setValue(final long value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Basket basket = (Basket) o;
        return paid == basket.paid && locked == basket.locked && value == basket.value && Objects.equals(basketID, basket.basketID) && Objects.equals(basketItems, basket.basketItems) && Objects.equals(rewardItems, basket.rewardItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basketID, basketItems, rewardItems, paid, locked, value);
    }

    @Override
    public String toString() {
        return "Basket{" +
                "basketID=" + basketID +
                ", basketItems=" + basketItems +
                ", rewardItems=" + rewardItems +
                ", paid=" + paid +
                ", locked=" + locked +
                ", value=" + value +
                '}';
    }
}
