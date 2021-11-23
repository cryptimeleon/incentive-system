package org.cryptimeleon.incentive.promotion.model;

import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

/**
 * Dataclass that represent a basket with all information required for promotions.
 */
public class Basket {
    public final UUID basketId;
    public final List<BasketItem> basketItemList;

    public Basket(UUID basketId, List<BasketItem> basketItemList) {
        this.basketId = basketId;
        this.basketItemList = basketItemList;
    }

    /**
     * Function that transforms the basketId to a zn element given the zn
     * @param zn the zn to use
     * @return the basket id as zn element
     */
    public Zn.ZnElement getBasketId(Zn zn) {
        return zn.createZnElement(new BigInteger(basketId.toString().replace("-", ""), 16));
    }
}
