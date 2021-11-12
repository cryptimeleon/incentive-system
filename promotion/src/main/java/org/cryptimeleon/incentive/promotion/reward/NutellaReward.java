package org.cryptimeleon.incentive.promotion.reward;

import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Get one free nutella for 4 points
 */
public class NutellaReward extends Reward {

    @Override
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints) {
        // newPoints = 1 * oldPoints + (basketPoints - 4)
        // && newPoints >= 0
        return new TokenUpdateLeaf(
                "nutella-leaf",
                Vector.of(BigInteger.ZERO),
                Vector.of((BigInteger) null),
                Vector.of(BigInteger.ONE),
                Vector.of(basketPoints.get(0).subtract(BigInteger.valueOf(4)))
        );
    }

    @Override
    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        var newPoints = tokenPoints.get(0).add(basketPoints.get(0)).subtract(BigInteger.valueOf(4));
        return newPoints.compareTo(BigInteger.ZERO) >= 0 ? Optional.of(Vector.of(newPoints)) : Optional.empty();
    }

    @Override
    public RewardSideEffect getSideEffect() {
        return new RewardSideEffect("Free Nutella");
    }
}
