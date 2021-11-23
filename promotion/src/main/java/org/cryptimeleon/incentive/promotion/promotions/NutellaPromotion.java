package org.cryptimeleon.incentive.promotion.promotions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.promotion.reward.NutellaReward;
import org.cryptimeleon.incentive.promotion.reward.Reward;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.ObjectRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This promotion is a classic promotion from the paper:
 * The point vector has only one entry, and users earn one point per nutella item.
 */
@Value
@AllArgsConstructor
public class NutellaPromotion implements Promotion {

    @Represented
    @NonFinal
    PromotionParameters promotionParameters;

    @Represented
    @NonFinal
    List<Reward> rewards;

    public NutellaPromotion(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    public static PromotionParameters generatePromotionParameters() {
        return IncentiveSystem.generatePromotionParameters(1);
    }

    public Vector<BigInteger> computeEarningsForBasket(Basket basket) {
        // TODO replace this by some kind of representable selector
        return Vector.of(BigInteger.valueOf(
                basket.getBasketItemList().stream()
                        .filter(basketItem -> basketItem.getTitle().toLowerCase().contains("nutella"))
                        .map(BasketItem::getCount)
                        .reduce(Integer::sum)
                        .orElseThrow()
        ));
    }

    public List<Reward> computeRewardsForPoints(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        return this.rewards.stream()
                .filter(reward -> reward.computeSatisfyingNewPointsVector(tokenPoints, basketPoints).isPresent())
                .collect(Collectors.toList());
    }

    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
