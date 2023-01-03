package org.cryptimeleon.incentive.promotion.model;

import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Dataclass that represent a basket with all information required for promotions.
 */
public final class Basket {
    // The id of the basket
    private final UUID basketId;
    // A list of all items in the basket
    private final List<BasketItem> basketItemList;

    public Basket(UUID basketId, List<BasketItem> basketItemList) {
        this.basketId = basketId;
        this.basketItemList = basketItemList;
    }

    /**
     * Function that transforms the basketId to a zn element given the zn
     *
     * @param zn the zn to use
     * @return the basket id as zn element
     */
    public Zn.ZnElement getBasketId(Zn zn) {
        return zn.createZnElement(new BigInteger(basketId.toString().replace("-", ""), 16));
    }

    /**
     * Helper function that sums up the costs of all basket items.
     *
     * @return cost of the basket
     */
    public int computeBasketValue() {
        return basketItemList.stream().mapToInt(basketItem -> basketItem.getCount() * basketItem.getPrice()).sum();
    }

    public UUID getBasketId() {
        return this.basketId;
    }

    public List<BasketItem> getBasketItemList() {
        return this.basketItemList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Basket basket = (Basket) o;
        return Objects.equals(basketId, basket.basketId) && Objects.equals(basketItemList, basket.basketItemList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basketId, basketItemList);
    }

    public String toString() {
        return "Basket(basketId=" + this.getBasketId() + ", basketItemList=" + this.getBasketItemList() + ")";
    }
}
