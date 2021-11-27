package org.cryptimeleon.incentive.promotion.promotions;

import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.reward.Reward;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Promotion objects completely determine the semantic of a promotion, how the token looks like, for what a user can earn points,
 * and which rewards a user can obtain.
 */
public interface Promotion extends StandaloneRepresentable {

    /**
     * Hacky way of requiring a static function that generates promotion parameters for this promotion class.
     *
     * @return promotion parameters
     */
    static PromotionParameters generatePromotionParameters() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Returns the promotion parameters for tokens of this reward.
     *
     * @return promotion parameters
     */
    PromotionParameters getPromotionParameters();

    /**
     * Computes how many points a basket is worth
     *
     * @param basket the basket to analyze
     * @return a vector of points
     */
    Vector<BigInteger> computeEarningsForBasket(Basket basket);

    /**
     * Computes which rewards the points in the token combined with the worth of the basket (determined with
     * {@literal computeEarningsForBasket}) qualify for.
     *
     * @param tokenPoints  points of the token
     * @param basketPoints worth of the basket
     * @return list of rewards
     */
    default List<Reward> computeRewardsForPoints(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        return this.getRewards().stream()
                .filter(reward -> reward.computeSatisfyingNewPointsVector(tokenPoints, basketPoints).isPresent())
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of all rewards a user can redeem with this token
     *
     * @return list of rewards.
     */
    List<Reward> getRewards();
}
