package org.cryptimeleon.incentive.promotion.promotions;

import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.reward.Reward;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Promotion objects completely determine the semantic of a promotion, how the token looks like, for what a user can earn points,
 * and which rewards a user can obtain.
 */
public abstract class Promotion implements StandaloneRepresentable {

    @Represented
    private PromotionParameters promotionParameters;
    @Represented
    private String promotionName;
    @Represented
    private String promotionDescription;
    @Represented
    private List<Reward> rewards;

    /**
     * Hacky way of requiring a static function that generates promotion parameters for this promotion class.
     *
     * @return promotion parameters
     */
    public static PromotionParameters generatePromotionParameters() {
        throw new RuntimeException("Not implemented");
    }

    public Promotion() {
    }

    public Promotion(PromotionParameters promotionParameters, String promotionName, String promotionDescription, List<Reward> rewards) {
        this.promotionParameters = promotionParameters;
        this.promotionName = promotionName;
        this.promotionDescription = promotionDescription;
        this.rewards = rewards;
    }


    /**
     * Computes how many points a basket is worth
     *
     * @param basket the basket to analyze
     * @return a vector of points
     */
    public abstract Vector<BigInteger> computeEarningsForBasket(Basket basket);

    /**
     * Computes which rewards the points in the token combined with the worth of the basket (determined with
     * {@literal computeEarningsForBasket}) qualify for.
     *
     * @param tokenPoints  points of the token
     * @param basketPoints worth of the basket
     * @return list of rewards
     */
    public List<Reward> computeRewardsForPoints(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        return this.getRewards().stream()
                .filter(reward -> reward.computeSatisfyingNewPointsVector(tokenPoints, basketPoints).isPresent())
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

    public List<Reward> getRewards() {
        return this.rewards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Promotion)) return false;
        Promotion promotion = (Promotion) o;
        return Objects.equals(promotionParameters, promotion.promotionParameters) && Objects.equals(promotionName, promotion.promotionName) && Objects.equals(promotionDescription, promotion.promotionDescription) && Objects.equals(rewards, promotion.rewards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promotionParameters, promotionName, promotionDescription, rewards);
    }
}
