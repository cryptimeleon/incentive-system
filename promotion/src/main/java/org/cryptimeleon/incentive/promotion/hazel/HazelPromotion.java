package org.cryptimeleon.incentive.promotion.hazel;

import lombok.EqualsAndHashCode;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;

/**
 * This promotion is a classic promotion from the paper:
 * The point vector has only one entry, and users earn one point per item that matches a selector (i.e. contain a string).
 */
@EqualsAndHashCode(callSuper = true)
public class HazelPromotion extends Promotion {

    @Represented
    private String selector;

    public HazelPromotion(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    /**
     * Constructor.
     *
     * @param promotionParameters  the promotion parameters
     * @param promotionName        a short name for the promotion
     * @param promotionDescription a brief description of the promotion and its rewards
     * @param zkpTokenUpdates      token updates for the promotion.
     * @param selector             the selector determines which items apply to the promotion. The case is ignored, any item
     *                             containing the selector in its title is counted
     */
    public HazelPromotion(PromotionParameters promotionParameters, String promotionName, String promotionDescription, List<ZkpTokenUpdate> zkpTokenUpdates, String selector) {
        super(promotionParameters, promotionName, promotionDescription, zkpTokenUpdates, true);
        this.selector = selector.toLowerCase();
    }

    public static PromotionParameters generatePromotionParameters() {
        return IncentiveSystem.generatePromotionParameters(1);
    }

    /**
     * Count items in basket that match the selector. Can be used to validate Earn-request server-side, and to include points to earn in zkp updates.
     *
     * @param basket the basket to analyze
     * @return a one-element vector with the count
     */
    public Vector<BigInteger> computeEarningsForBasket(Basket basket) {
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
}
