package org.cryptimeleon.incentive.promotion.streak;

import lombok.EqualsAndHashCode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductOrNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

/**
 * Streak update that allows the user to anonymously update tokens if they are within the streak, and reset it if not within the range.
 * It is kept secret which of the two updates is performed.
 */
@EqualsAndHashCode(callSuper = true)
public class StandardStreakTokenUpdate extends StreakZkpTokenUpdate {
    public StandardStreakTokenUpdate(Representation representation) {
        super(representation);
    }

    public StandardStreakTokenUpdate(UUID rewardId, String rewardDescription, RewardSideEffect rewardSideEffect, int intervalDays) {
        super(rewardId, rewardDescription, rewardSideEffect, intervalDays);
    }

    @Override
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        long now = toTimestampMetadata(zkpTokenUpdateMetadata).getTimestamp();
        return new SpendDeductOrNode(updateTree(now), resetTree(now));
    }

    @Override
    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        long now = toTimestampMetadata(zkpTokenUpdateMetadata).getTimestamp();

        if (now - tokenPoints.get(1).longValueExact() > intervalDays) {
            // Streak lost, update to 1
            return Optional.of(Vector.of(BigInteger.ONE, BigInteger.valueOf(now)));
        } else {
            return Optional.of(Vector.of(tokenPoints.get(0).add(BigInteger.ONE), BigInteger.valueOf(now)));
        }
    }
}
