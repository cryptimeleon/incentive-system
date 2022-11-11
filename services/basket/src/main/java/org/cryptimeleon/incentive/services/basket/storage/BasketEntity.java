package org.cryptimeleon.incentive.services.basket.storage;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "baskets")
public class BasketEntity {
    @Id
    private UUID basketID;
    @OneToMany
    private Set<ItemInBasketEntity> basketItems = new HashSet<>();
    @ElementCollection
    private Set<String> rewardItems = new HashSet<>();
    private boolean paid;
    private boolean redeemed;
    private boolean locked;
    private String redeemRequest;
    // private long value;

    public BasketEntity() {
        this.basketID = UUID.randomUUID();
    }

    public UUID getBasketID() {
        return basketID;
    }

    public void setBasketID(UUID basketID) {
        this.basketID = basketID;
    }

    public Set<ItemInBasketEntity> getBasketItems() {
        return basketItems;
    }

    public void setBasketItems(Set<ItemInBasketEntity> basketItems) {
        this.basketItems = basketItems;
    }

    public void addBasketItem(ItemEntity itemEntity, int count) {
        var basketItemEntity = new ItemInBasketEntity(this, itemEntity);
        basketItemEntity.setCount(count);
        this.basketItems.add(basketItemEntity);
    }

    public Set<String> getRewardItems() {
        return rewardItems;
    }

    public void setRewardItems(Set<String> rewardItems) {
        this.rewardItems = rewardItems;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isRedeemed() {
        return redeemed;
    }

    public void setRedeemed(boolean redeemed) {
        this.redeemed = redeemed;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getRedeemRequest() {
        return redeemRequest;
    }

    public void setRedeemRequest(String redeemRequest) {
        this.redeemRequest = redeemRequest;
    }
}
