package org.cryptimeleon.incentive.promotion.reward;

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
public class NutellaReward extends Reward {

    @Represented
    private Integer rewardCost;

    public NutellaReward(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    public NutellaReward(Integer rewardCost, String rewardDescription, UUID rewardId, RewardSideEffect rewardSideEffect) {
        super(rewardId, rewardDescription, rewardSideEffect);
        this.rewardCost = rewardCost;
    }

    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints) {
        // && newPoints >= 0
        return new TokenUpdateLeaf(
                "nutella-leaf",
                Vector.of(BigInteger.ZERO),
                Vector.of((BigInteger) null),
                Vector.of(BigInteger.ONE),
                Vector.of(basketPoints.get(0).subtract(BigInteger.valueOf(rewardCost)))
        );
    }

    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        var newPoints = tokenPoints.get(0).add(basketPoints.get(0)).subtract(BigInteger.valueOf(rewardCost));
        return newPoints.compareTo(BigInteger.ZERO) >= 0 ? Optional.of(Vector.of(newPoints)) : Optional.empty();
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public Integer getRewardCost() {
        return this.rewardCost;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof NutellaReward)) return false;
        final NutellaReward other = (NutellaReward) o;
        if (!other.canEqual((Object) this)) return false;
        if (!super.equals(o)) return false;
        final Object this$rewardCost = this.getRewardCost();
        final Object other$rewardCost = other.getRewardCost();
        if (this$rewardCost == null ? other$rewardCost != null : !this$rewardCost.equals(other$rewardCost))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof NutellaReward;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final Object $rewardCost = this.getRewardCost();
        result = result * PRIME + ($rewardCost == null ? 43 : $rewardCost.hashCode());
        return result;
    }
}
