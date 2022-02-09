package org.cryptimeleon.incentive.crypto.proof.spend.leaf;

import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductLeafNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;

/**
 * Leaf node for representing range proofs over the new points vector of the old token combined with affine linear relations
 * between the points of the old and new token.
 * <p>
 * The statement has the form:
 * {@literal for all i: lowerLimits[i] <= newPointsVector[i] <= upperLimits[i]
 * and newPointsVector[i] = aVector[i] * oldVector[i] + bVector[i]`}
 * <p>
 * Also supports only partial range proofs and partial affine linear relation proofs by setting all other entries to null.
 */
public class TokenUpdateLeaf extends SpendDeductLeafNode {

    public final Vector<BigInteger> lowerLimits;
    public final Vector<BigInteger> upperLimits;
    public final Vector<BigInteger> aVector;
    public final Vector<BigInteger> bVector;

    /**
     * Constructor with uninitialized hasWitness field, to be used by the verifier who neither knows nor needs that attribute.
     * Set members to null to indicate that no proof should be performed for that index.
     *
     * @param leafName    a name that uniquely identifies this leaf in a {@link SpendDeductTree}.
     * @param lowerLimits an array of greater than or equal relations of the new points vector
     * @param upperLimits an array of less than or equal relations of the new points vector
     * @param aVector     an array of coefficients a for the affine linear relation proofs.
     * @param bVector     an array of summands a for the affine linear relation proofs.
     */
    public TokenUpdateLeaf(String leafName, Vector<BigInteger> lowerLimits, Vector<BigInteger> upperLimits, Vector<BigInteger> aVector, Vector<BigInteger> bVector) {
        super(leafName);
        this.lowerLimits = lowerLimits;
        this.upperLimits = upperLimits;
        this.aVector = aVector;
        this.bVector = bVector;
    }

    @Override
    public boolean isValidForPoints(Vector<BigInteger> oldPointsVector, Vector<BigInteger> newPointsVector) {
        boolean isValid = Util.arePointsInRange(newPointsVector, this.lowerLimits, this.upperLimits);
        for (int i = 0; i < oldPointsVector.length(); i++) {
            isValid &= newPointsVector.get(i).equals(this.aVector.get(i).multiply(oldPointsVector.get(i)).add(this.bVector.get(i)));
        }
        return isValid;
    }

}
