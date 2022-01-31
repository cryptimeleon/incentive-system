package org.cryptimeleon.incentive.promotion.streak;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

/**
 * Update for which only strike updates to a value larger than or equal to lower limit are valid.
 * Can be used for VIP like promotions where all users that have a large enough streak get a reward while on that streak.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class RangeProofStreakTokenUpdate extends StreakZkpTokenUpdate {

    // Lower limit for getting reward for this streak
    @Represented
    Integer lowerLimit;

    public RangeProofStreakTokenUpdate(Representation representation) {
        super(representation);
    }

    public RangeProofStreakTokenUpdate(UUID rewardId, String rewardDescription, RewardSideEffect rewardSideEffect, int intervalDays, int lowerLimit) {
        super(rewardId, rewardDescription, rewardSideEffect, intervalDays);
        this.lowerLimit = lowerLimit;
    }

    /**
     * @param basketPoints           a vector representing the points a user can earn for this basket
     * @param zkpTokenUpdateMetadata
     * @return a spend-deduct tree from which the ZKP that the user must provide can be generated
     */
    @Override
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        long now = toTimestampMetadata(zkpTokenUpdateMetadata).getTimestamp();
        return updateTreeRangeProof(now, lowerLimit);
    }

    /**
     * @param tokenPoints            the points of the token
     * @param basketPoints           the points that the basket is worth
     * @param zkpTokenUpdateMetadata
     * @return and optional vector, which returns satisfying points vector, or empty if none was found
     */
    @Override
    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        long now = toTimestampMetadata(zkpTokenUpdateMetadata).getTimestamp();

        if (now - tokenPoints.get(1).longValueExact() <= intervalDays && tokenPoints.get(0).intValueExact() + 1 >= lowerLimit) {
            return Optional.of(Vector.of(tokenPoints.get(0).add(BigInteger.ONE), BigInteger.valueOf(now)));
        }

        return Optional.empty();
    }
}
