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
    @OneToMany(cascade = CascadeType.ALL) // Store children with this entity
    private Set<ItemInBasketEntity> basketItems = new HashSet<>();
    @OneToMany
    private Set<RewardItemEntity> rewardItems = new HashSet<>();
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

    public Set<ItemInBasketEntity> getBasketItems() {
        return basketItems;
    }

    public void addBasketItem(ItemEntity itemEntity, int count) {
        var id = new ItemInBasketId(this.getBasketID(), itemEntity.getId());
        var itemOptional = this.basketItems.stream().filter(e -> e.getId().equals(id)).findAny();

        // Sets cannot overwrite elements.
        if (itemOptional.isPresent()) {
            itemOptional.get().setCount(count);
        } else {
            var basketItemEntity = new ItemInBasketEntity(this, itemEntity);
            basketItemEntity.setCount(count);

            var success = basketItems.add(basketItemEntity);
            assert success;
        }
    }

    public void removeBasketItem(ItemEntity itemEntity) {
        basketItems.removeIf(e -> e.getItem().equals(itemEntity));
    }

    public Set<RewardItemEntity> getRewardItems() {
        return rewardItems;
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
