package org.cryptimeleon.incentive.services.basket.storage;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.*;

@Entity
@Table(name = "baskets")
public class BasketEntity {
    @Id
    private UUID basketID;
    @OneToMany(cascade = CascadeType.ALL) // Store children with this entity
    private Set<ItemInBasketEntity> basketItems = new HashSet<>();
    @ManyToMany
    private Set<RewardItemEntity> rewardItems = new HashSet<>();
    private boolean paid;
    private boolean redeemed;
    private boolean locked;
    private String redeemRequest;

    @ElementCollection
    private Map<BigInteger, byte[]> redeemRequestHashes  = new HashMap<>();

    public BasketEntity() {
        this.basketID = UUID.randomUUID();
    }

    public BasketEntity(UUID basketID, Set<ItemInBasketEntity> basketItems, Set<RewardItemEntity> rewardItems, boolean paid, boolean redeemed, boolean locked, String redeemRequest, HashMap<BigInteger, byte[]> redeemRequestHashes) {
        this.basketID = basketID;
        this.basketItems = basketItems;
        this.rewardItems = rewardItems;
        this.paid = paid;
        this.redeemed = redeemed;
        this.locked = locked;
        this.redeemRequest = redeemRequest;
        this.redeemRequestHashes = redeemRequestHashes;
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

    public Optional<byte[]> getRedeemHashForPromotionId(BigInteger promotionId) {
        if (redeemRequestHashes.containsKey(promotionId)) return Optional.of(redeemRequestHashes.get(promotionId));
        return Optional.empty();
    }

    public void setRedeemHashForPromotionId(BigInteger promotionId, byte[] hash) {
        redeemRequestHashes.put(promotionId, hash);
    }
}
