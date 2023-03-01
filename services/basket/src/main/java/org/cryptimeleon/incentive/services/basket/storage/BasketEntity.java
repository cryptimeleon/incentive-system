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
    private boolean locked;

    @ElementCollection
    @MapKeyColumn(name = "key", columnDefinition = "DECIMAL(50,0)") // Allow more digits than default for key (19)
    private Map<BigInteger, byte[]> redeemRequestHashes = new HashMap<>();

    public BasketEntity() {
        this.basketID = UUID.randomUUID();
    }

    public BasketEntity(UUID basketID, Set<ItemInBasketEntity> basketItems, Set<RewardItemEntity> rewardItems, boolean paid, boolean locked, HashMap<BigInteger, byte[]> redeemRequestHashes) {
        this.basketID = basketID;
        this.basketItems = basketItems;
        this.rewardItems = rewardItems;
        this.paid = paid;
        this.locked = locked;
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

    public void addRewardItems(List<RewardItemEntity> rewardItemEntities) {
        rewardItems.addAll(rewardItemEntities);
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Optional<byte[]> getRedeemHashForPromotionId(BigInteger promotionId) {
        if (redeemRequestHashes.containsKey(promotionId)) return Optional.of(redeemRequestHashes.get(promotionId));
        return Optional.empty();
    }

    public void setRedeemHashForPromotionId(BigInteger promotionId, byte[] hash) {
        redeemRequestHashes.put(promotionId, hash);
    }
}
