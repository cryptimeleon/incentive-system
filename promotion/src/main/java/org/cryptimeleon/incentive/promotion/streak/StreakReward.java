package org.cryptimeleon.incentive.promotion.streak;

import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductOrNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.Reward;
import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

/**
 * A reward that updates the streak and offers possible side effects depending on the instantiation.
 * E.g. if streak < 5 then update streak, if streak >= 5 then update streak and give a discount of 2%
 */
public class StreakReward extends Reward {

    @Represented
    private Integer interval;
    private StreakTimeUtil streakTimeUtil;

    public StreakReward(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    public StreakReward(UUID rewardId, String rewardDescription, RewardSideEffect rewardSideEffect, Integer interval) {
        super(rewardId, rewardDescription, rewardSideEffect);
        this.interval = interval;
        this.streakTimeUtil = new StreakTimeUtil();
    }

    public StreakReward(UUID rewardId, String rewardDescription, RewardSideEffect rewardSideEffect, Integer interval, StreakTimeUtil streakTimeUtil) {
        super(rewardId, rewardDescription, rewardSideEffect);
        this.interval = interval;
        this.streakTimeUtil = streakTimeUtil;
    }

    @Override
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints) {
        long todayAsEpoch = streakTimeUtil.getTodayAsEpochDay();
        return new SpendDeductOrNode(
                // Case in which user can update streak
                new SpendDeductAndNode(
                        new TokenPointsLeaf("previous-in-range",
                                Vector.of(null, BigInteger.valueOf(todayAsEpoch - interval)),
                                Vector.of((BigInteger) null, null)),
                        new TokenUpdateLeaf("update-in-range",
                                Vector.of((BigInteger) null, null),
                                Vector.of((BigInteger) null, null),
                                Vector.of(BigInteger.ONE, BigInteger.ZERO),
                                Vector.of(BigInteger.ONE, BigInteger.valueOf(todayAsEpoch)))),
                // User needs to restart streak: Start from zero!
                new SpendDeductAndNode(
                        // This PointsLeaf might be removed in the future, right now it only ensures that users only have a witness for either one of the Or children.
                        new TokenPointsLeaf("previous-not-in-range",
                                Vector.of((BigInteger) null, null),
                                Vector.of(null, BigInteger.valueOf(todayAsEpoch - interval - 1))),
                        new TokenUpdateLeaf("start-from-zero",
                                Vector.of((BigInteger) null, null),
                                Vector.of((BigInteger) null, null),
                                Vector.of(BigInteger.ZERO, BigInteger.ZERO),
                                Vector.of(BigInteger.ONE, BigInteger.valueOf(todayAsEpoch)))
                )
        );
    }

    @Override
    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        // Ignore basketPoints, we are only interested in day of shopping
        long todayAsEpoch = streakTimeUtil.getTodayAsEpochDay();
        if (todayAsEpoch - tokenPoints.get(1).longValueExact() > interval) {
            // Streak lost, update to 1
            return Optional.of(Vector.of(BigInteger.ONE, BigInteger.valueOf(todayAsEpoch)));
        } else {
            return Optional.of(Vector.of(tokenPoints.get(0).add(BigInteger.ONE), BigInteger.valueOf(todayAsEpoch)));
        }
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
