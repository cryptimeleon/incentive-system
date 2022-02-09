package org.cryptimeleon.incentive.promotion.promotions;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.promotion.reward.Reward;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * This promotion is a classic promotion from the paper:
 * The point vector has only one entry, and users earn one point per nutella item.
 */
public class NutellaPromotion extends Promotion {

    @Represented
    private String selector;

    public NutellaPromotion(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    public NutellaPromotion(PromotionParameters promotionParameters, String promotionName, String promotionDescription, List<Reward> rewards, String selector) {
        super(promotionParameters, promotionName, promotionDescription, rewards);
        this.selector = selector.toLowerCase();
    }

    public static PromotionParameters generatePromotionParameters() {
        return IncentiveSystem.generatePromotionParameters(1);
    }

    public Vector<BigInteger> computeEarningsForBasket(Basket basket) {
        // TODO replace this by some kind of representable selector
        return Vector.of(BigInteger.valueOf(
                basket.getBasketItemList().stream()
                        .filter(basketItem -> basketItem.getTitle().toLowerCase().contains(selector))
                        .map(BasketItem::getCount)
                        .reduce(Integer::sum)
                        .orElseThrow()
        ));
    }

    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NutellaPromotion that = (NutellaPromotion) o;
        return Objects.equals(selector, that.selector);
    }

    public int hashCode() {
        return Objects.hash(super.hashCode(), selector);
    }
}
