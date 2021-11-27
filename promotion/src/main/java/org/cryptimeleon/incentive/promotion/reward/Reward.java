package org.cryptimeleon.incentive.promotion.reward;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

/**
 * A reward object identifies a reward and the conditions in the form of a ZKP relation.
 */
public interface Reward extends StandaloneRepresentable {

    /**
     * Generate the tree that represent the partial proof of knowledge that is required to get the reward.
     * The basket points vector represents what the current basket is worth, and can be offset with the token points.
     * For example, if a user has 3 points on the token, the basket is worth 2 points, and the reward required 4 points
     * then the user can get 1 point with the reward, instead of having too little points.
     *
     * @param basketPoints a vector representing the points a user can earn for this basket
     * @return a spend-deduct tree from which the ZKP that the user must provide can be generated
     */
    SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints);

    /**
     * We use partial proofs of knowledge in the underlaying crypto api, with statements that could become quite
     * powerful. Part of the witnesss required to satisfy the generated ZKP relations are the points that the new token has.
     * Determining such a vector, or even if it exists, can be non-trivial, hence we provide this function that must be
     * implemented fa reward.
     * The function returns such a points vector, or Optional.empty if none was found.
     *
     * @param tokenPoints  the points of the token
     * @param basketPoints the points that the basket is worth
     * @return and optional vector, which returns satisfying points vector, or empty if none was found
     */
    Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints);

    /**
     * This function returns a representation of the actual reward.
     *
     * @return a representation of the reward.
     */
    RewardSideEffect getSideEffect();

    /**
     * Returns a random id that uniquely determines the reward.
     *
     * @return the reward id
     */
    UUID getRewardId();
}
