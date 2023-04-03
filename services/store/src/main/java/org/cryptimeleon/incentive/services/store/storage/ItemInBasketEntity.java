package org.cryptimeleon.incentive.services.store.storage;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "item_in_basket")
public class ItemInBasketEntity {
    @EmbeddedId
    private ItemInBasketId id;

    @ManyToOne
    @MapsId("basketId")
    @JoinColumn(name = "basket_id")
    private BasketEntity basket;

    @ManyToOne
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private ItemEntity item;
    private int count;

    public ItemInBasketEntity() {

    }

    public ItemInBasketEntity(BasketEntity basketEntity, ItemEntity itemEntity) {
        this.item = itemEntity;
        this.basket = basketEntity;
        this.id = new ItemInBasketId(basketEntity.getBasketID(), itemEntity.getId());
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }


    public ItemInBasketId getId() {
        return id;
    }

    public BasketEntity getBasket() {
        return basket;
    }

    public void setBasket(BasketEntity basket) {
        this.basket = basket;
    }

    public ItemEntity getItem() {
        return item;
    }

    public void setItem(ItemEntity item) {
        this.item = item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemInBasketEntity that = (ItemInBasketEntity) o;
        return Objects.equals(basket, that.basket) && Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basket, item);
    }
}

