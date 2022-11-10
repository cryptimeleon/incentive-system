package org.cryptimeleon.incentive.services.basket.storage;

import javax.persistence.*;

@Entity
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
}

