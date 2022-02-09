package org.cryptimeleon.incentive.crypto.proof.spend.leaf;

import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductLeafNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;

/**
 * Leaf node for representing range proofs over the points vector of the old token.
 * The statement has the form {@literal `for all i: lowerLimits[i] <= pointsVector[i] <= upperLimits[i]`}
 * Also supports only partial range proofs by setting all other entries to null.
 */
public class TokenPointsLeaf extends SpendDeductLeafNode {
    public final Vector<BigInteger> lowerLimits;
    public final Vector<BigInteger> upperLimits;

    /**
     * Constructor with uninitialized hasWitness field, to be used by the verifier who neither knows nor needs that attribute.
     * Set members to null to indicate that no proof should be performed for that index.
     *
     * @param leafName    a name that uniquely identifies this leaf in a {@link SpendDeductTree}.
     * @param lowerLimits an array of greater than or equal relations to show.
     * @param upperLimits an array of less than or equal relations to show.
     */
    public TokenPointsLeaf(String leafName, Vector<BigInteger> lowerLimits, Vector<BigInteger> upperLimits) {
        super(leafName);
        this.lowerLimits = lowerLimits;
        this.upperLimits = upperLimits;
    }

    @Override
    public boolean isValidForPoints(Vector<BigInteger> pointsVector, Vector<BigInteger> newPointsVector) {
        return Util.arePointsInRange(pointsVector, this.lowerLimits, this.upperLimits);
    }
}
