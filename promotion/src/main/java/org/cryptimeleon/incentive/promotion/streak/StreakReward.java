package org.cryptimeleon.incentive.promotion.streak;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.Reward;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;

/**
 * A reward that updates the streak and offers possible side effects depending on the instantiation.
 * E.g. if streak < 5 then update streak, if streak >= 5 then update streak and give a discount of 2%
 */
public class StreakReward extends Reward {

    @Override
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints) {
        return null;
    }

    @Override
    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        return Optional.empty();
    }

    @Override
    public Representation getRepresentation() {
        return null;
    }
}
