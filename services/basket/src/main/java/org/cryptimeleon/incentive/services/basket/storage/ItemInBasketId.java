package org.cryptimeleon.incentive.services.basket.storage;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ItemInBasketId implements Serializable {

    @Column(name = "basket_id")
    private UUID basketId;

    @Column(name = "item_id")
    private String itemId;

    public ItemInBasketId(UUID basketID, String itemId) {
        this.basketId = basketID;
        this.itemId = itemId;
    }

    public ItemInBasketId() {
    }

    public UUID getBasketId() {
        return basketId;
    }

    public void setBasketId(UUID basketId) {
        this.basketId = basketId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemInBasketId that = (ItemInBasketId) o;
        return Objects.equals(basketId, that.basketId) && Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basketId, itemId);
    }
}
