package org.cryptimeleon.incentive.promotion.streak;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

/**
 * Token updates that in addition to maintaining a streak spend some of the streak points to get some reward.
 * This update does not allow streak resets as {@link StandardStreakTokenUpdate}.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class SpendStreakTokenUpdate extends StreakZkpTokenUpdate {

    // A reward is traded for some streak points
    @Represented
    Integer cost;

    public SpendStreakTokenUpdate(Representation representation) {
        super(representation);
    }

    /**
     * Constructor.
     *
     * @param rewardId          every reward is identified by a unique id. This is for example useful for the user to
     *                          tell the server which update it should verify
     * @param rewardDescription a short description text on what this ZKP update actually does to display in an application on the user side
     * @param rewardSideEffect  the side effect of this update
     * @param intervalDays      the interval in which the streak needs to be updates to not get lost.
     * @param cost              the costs of the side effect in streak points
     */
    public SpendStreakTokenUpdate(UUID rewardId, String rewardDescription, RewardSideEffect rewardSideEffect, int intervalDays, int cost) {
        super(rewardId, rewardDescription, rewardSideEffect, intervalDays);
        this.cost = cost;
    }

    /**
     * Generate the statement of this update based on user input (basket and metadata).
     * Only permits updates on the streak that then subtract {@link #cost} points.
     *
     * @param basketPoints           a vector representing the points a user can earn for this basket
     * @param zkpTokenUpdateMetadata the timestamp sent by the user used to generate the non-interactive proof.
     * @return a spend-deduct tree from which the ZKP that the user must provide can be generated
     */
    @Override
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        long now = toTimestampMetadata(zkpTokenUpdateMetadata).getTimestamp();
        return updateTreeAndSpend(now, cost);
    }

    /**
     * Compute whether a user can apply this update and how many points the token holds afterwards.
     *
     * @param tokenPoints            the points of the token
     * @param basketPoints           the points that the basket is worth
     * @param zkpTokenUpdateMetadata a current timestamp
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
