package org.cryptimeleon.incentive.promotion;

import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;

/**
 * Promotion objects completely determine the semantic of a promotion, how the token looks like, for what a user can earn points,
 * and which rewards a user can obtain.
 */
@EqualsAndHashCode
public abstract class Promotion implements StandaloneRepresentable {

    @Represented
    private PromotionParameters promotionParameters;
    @Represented
    private String promotionName;
    @Represented
    private String promotionDescription;
    @Represented
    private List<ZkpTokenUpdate> zkpTokenUpdates;

    public Promotion() {
    }

    public Promotion(PromotionParameters promotionParameters, String promotionName, String promotionDescription, List<ZkpTokenUpdate> zkpTokenUpdates) {
        this.promotionParameters = promotionParameters;
        this.promotionName = promotionName;
        this.promotionDescription = promotionDescription;
        this.zkpTokenUpdates = zkpTokenUpdates;
    }

    /**
     * Hacky way of requiring a static function that generates promotion parameters for this promotion class.
     *
     * @return promotion parameters
     */
    public static PromotionParameters generatePromotionParameters() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Computes how many points a basket is worth in terms of simple 'Earn' updates, i.e. adding a vector to to token's
     * point vector. Some promotions might not utilize this feature because they require ZKPs for all updates on tokens.
     *
     * @param basket the basket to analyze
     * @return a vector of points
     */
    public abstract Vector<BigInteger> computeEarningsForBasket(Basket basket);

    /**
     * Computes which rewards the points in the token combined with the worth of the basket (determined with
     * {@literal computeEarningsForBasket}) qualify for.
     *
     * @param tokenPoints            points of the token
     * @param basketPoints           worth of the basket
     * @param zkpTokenUpdateMetadata
     * @return list of rewards
     */
    public List<ZkpTokenUpdate> computeRewardsForPoints(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        return this.getTokenUpdates().stream()
                .filter(update -> update.computeSatisfyingNewPointsVector(tokenPoints, basketPoints, zkpTokenUpdateMetadata).isPresent())
                .collect(Collectors.toList());
    }

    public PromotionParameters getPromotionParameters() {
        return this.promotionParameters;
    }

    public String getPromotionName() {
        return this.promotionName;
    }

    public String getPromotionDescription() {
        return this.promotionDescription;
    }

    public List<ZkpTokenUpdate> getTokenUpdates() {
        return this.zkpTokenUpdates;
    }
}
