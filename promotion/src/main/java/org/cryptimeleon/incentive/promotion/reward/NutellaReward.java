package org.cryptimeleon.incentive.promotion.reward;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

/**
 * Get one free nutella for rewardCost points
 */
@Value
@AllArgsConstructor
public class NutellaReward implements Reward {

    @Represented
    @NonFinal
    Integer rewardCost;

    @Represented
    @NonFinal
    UUID rewardId;

    @Represented
    @NonFinal
    RewardSideEffect rewardSideEffect;

    public NutellaReward(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    @Override
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints) {
        // newPoints = 1 * oldPoints + (basketPoints - 4)
        // && newPoints >= 0
        return new TokenUpdateLeaf(
                "nutella-leaf",
                Vector.of(BigInteger.ZERO),
                Vector.of((BigInteger) null),
                Vector.of(BigInteger.ONE),
                Vector.of(basketPoints.get(0).subtract(BigInteger.valueOf(rewardCost)))
        );
    }

    @Override
    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        var newPoints = tokenPoints.get(0).add(basketPoints.get(0)).subtract(BigInteger.valueOf(rewardCost));
        return newPoints.compareTo(BigInteger.ZERO) >= 0 ? Optional.of(Vector.of(newPoints)) : Optional.empty();
    }

    @Override
    public RewardSideEffect getSideEffect() {
        return new RewardSideEffect("Free Nutella");
    }

    @Override
    public UUID getRewardId() {
        return this.rewardId;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
