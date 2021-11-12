package org.cryptimeleon.incentive.promotion.model;

import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class Basket {
    public final UUID basketId;
    public final List<BasketItem> basketItemList;

    public Basket(UUID basketId, List<BasketItem> basketItemList) {
        this.basketId = basketId;
        this.basketItemList = basketItemList;
    }

    public Zn.ZnElement getBasketId(Zn zn) {
        return zn.createZnElement(new BigInteger(basketId.toString().replace("-", ""), 16));
    }
}
