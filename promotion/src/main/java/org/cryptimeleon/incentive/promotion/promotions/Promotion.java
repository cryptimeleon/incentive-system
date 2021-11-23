package org.cryptimeleon.incentive.promotion.promotions;

import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.reward.Reward;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;

/**
 * Promotion objects completely determine the semantic of a promotion, how the token looks like, for what a user can earn points,
 * and which rewards a user can obtain.
 */
public interface Promotion extends Representable {

    PromotionParameters getPromotionParameters();

    Vector<BigInteger> computeEarningsForBasket(Basket basket);

    List<Reward> computeRewardsForPoints(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints);

    List<Reward> getRewards();

    static PromotionParameters generatePromotionParameters() {
        throw new RuntimeException("Not implemented");
    }
}
