package org.cryptimeleon.incentive.promotion.streak;

import lombok.EqualsAndHashCode;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.Reward;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;

/**
 * Promotion that counts the number of cumulative n-day intervals in which the user bought something.
 * Token format: (streak-count, last-visited-epoch)
 */
@EqualsAndHashCode(callSuper = true)
public class StreakPromotion extends Promotion {

    // Number of days until streak ends
    @Represented
    private Integer interval;

    public StreakPromotion(PromotionParameters promotionParameters, String promotionName, String promotionDescription, List<Reward> rewards, int interval) {
        super(promotionParameters, promotionName, promotionDescription, rewards);
        this.interval = interval;
    }

    public StreakPromotion(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    /**
     * Generates PromotionParameters for a new StreakPromotion
     *
     * @return the PromotionParameters
     */
    public static PromotionParameters generatePromotionParameters() {
        return IncentiveSystem.generatePromotionParameters(2);
    }

    @Override
    public Vector<BigInteger> computeEarningsForBasket(Basket basket) {
        // Cannot simply earn, always needs to check currentDate - lastDate <= interval
        return Vector.of(BigInteger.ZERO, BigInteger.ZERO);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public int getInterval() {
        return interval;
    }
}
