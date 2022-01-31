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

@Getter
@EqualsAndHashCode(callSuper = true)
public class SpendStreakTokenUpdate extends StreakZkpTokenUpdate {

    // A reward is traded for some streak points
    @Represented
    Integer cost;

    public SpendStreakTokenUpdate(Representation representation) {
        super(representation);
    }

    public SpendStreakTokenUpdate(UUID rewardId, String rewardDescription, RewardSideEffect rewardSideEffect, int intervalDays, int cost) {
        super(rewardId, rewardDescription, rewardSideEffect, intervalDays);
        this.cost = cost;
    }

    /**
     * @param basketPoints           a vector representing the points a user can earn for this basket
     * @param zkpTokenUpdateMetadata
     * @return a spend-deduct tree from which the ZKP that the user must provide can be generated
     */
    @Override
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        long now = toTimestampMetadata(zkpTokenUpdateMetadata).getTimestamp();
        return updateTreeAndSpend(now, cost);
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

        if (now - tokenPoints.get(1).longValueExact() <= intervalDays && tokenPoints.get(0).intValueExact() + 1 >= cost) {
            return Optional.of(Vector.of(tokenPoints.get(0).add(BigInteger.valueOf(1 - cost)), BigInteger.valueOf(now)));
        }

        return Optional.empty();
    }
}
