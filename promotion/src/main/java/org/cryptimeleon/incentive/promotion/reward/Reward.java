package org.cryptimeleon.incentive.promotion.reward;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

/**
 * A reward object identifies a reward and the conditions in the form of a ZKP relation.
 */
public interface Reward extends Representable {
    SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints);

    Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints);

    RewardSideEffect getSideEffect();

    UUID getRewardId();
}
