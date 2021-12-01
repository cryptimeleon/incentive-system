package org.cryptimeleon.incentive.crypto.proof.spend.tree;

import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;

/**
 * Interface for building a tree structure for boolean statements.
 */
public interface SpendDeductTree {

    /**
     * Function that determines whether this tree's ZKP is satisfied by the given old and new points vectors.
     *
     * @param pointsVector    vector containing the old token's points
     * @param newPointsVector vector containing the points the users want for their new token
     * @return whether the point vectors satisfy the ZKP relation
     */
    boolean isValidForPoints(Vector<BigInteger> pointsVector, Vector<BigInteger> newPointsVector);
}
