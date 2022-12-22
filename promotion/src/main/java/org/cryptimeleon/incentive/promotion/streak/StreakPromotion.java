package org.cryptimeleon.incentive.promotion.streak;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Promotion that counts the number of cumulative n-day intervals in which the user bought something. This incentives
 * users to shop regularly in some store.
 * For this, it is checked that the last day of visiting the store is not more than n days in the past. This means that
 * one can also update the streak by shopping twice at the same day.
 * <p>
 * Token format: (streak-count, last-visited-epoch)
 * The promotion does not support the fast earn protocol since we always need a ZKP to check the last timestamp.
 */
public class StreakPromotion extends Promotion {

    // Number of days until streak ends
    @Represented
    private Integer interval;

    public StreakPromotion(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    /**
     * Constructor.
     *
     * @param promotionParameters  the promotion parameters of this promotion. This parameter should be generated with
     *                             the static function of this class and it is not verified that the vector size is correct!
     * @param promotionName        name of the promotion
     * @param promotionDescription a short description of the promotion
     * @param zkpTokenUpdates      a list of all possible updates that use ZKPs
     * @param interval             the interval in which a streak must be maintained
     */
    public StreakPromotion(PromotionParameters promotionParameters, String promotionName, String promotionDescription, List<ZkpTokenUpdate> zkpTokenUpdates, int interval) {
        super(promotionParameters, promotionName, promotionDescription, zkpTokenUpdates, false); // TODO StandardUpdate must always be present
        this.interval = interval;
    }

    /**
     * Generates PromotionParameters for a new StreakPromotion
     *
     * @return the PromotionParameters
     */
    public static PromotionParameters generatePromotionParameters() {
        return IncentiveSystem.generatePromotionParameters(2);
    }

    /**
     * This promotion does not need to analyze the basket, hence this will always return a zero vector.
     *
     * @param basket the basket to analyze
     * @return a vector of zeros
     */
    @Override
    public Vector<BigInteger> computeEarningsForBasket(Basket basket) {
        return Vector.of(BigInteger.ZERO, BigInteger.ZERO);
    }

    @Override
    public ZkpTokenUpdateMetadata generateMetadataForUpdate() {
        return StreakTokenUpdateTimestamp.now();
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public Integer getInterval() {
        return this.interval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StreakPromotion that = (StreakPromotion) o;
        return Objects.equals(interval, that.interval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), interval);
    }
}
