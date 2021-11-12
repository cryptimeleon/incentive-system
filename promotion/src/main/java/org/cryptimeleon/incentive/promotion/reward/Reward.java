package org.cryptimeleon.incentive.promotion.reward;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;

/**
 * A reward object identifies a reward and the conditions in the form of a ZKP relation.
 */
abstract public class Reward {

    public abstract SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints);

    public abstract Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints);

    public abstract RewardSideEffect getSideEffect();
}
