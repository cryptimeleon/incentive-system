package org.cryptimeleon.incentive.crypto.proof.spend.leaf;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductLeafNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;

/**
 * Leaf node for representing range proofs over the points vector of the old token combined with affine linear relations
 * between the points of the old and new token.
 * <p>
 * The statement has the form:
 * for all i: lowerLimits[i] <= oldPointsVector[i] <= upperLimits[i]
 * and newPointsVector[i] = aVector[i] * oldVector[i] + bVector[i]`
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
     * @param lowerLimits an array of greater than or equal relations to show.
     * @param upperLimits an array of less than or equal relations to show.
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

    /**
     * Alternative constructor that initializes the hasWitness field for use at the prover's side.
     * Set members to null to indicate that no proof should be performed for that index.
     *
     * @param leafName    a name that uniquely identifies this leaf in a {@link SpendDeductTree}.
     * @param lowerLimits an array of greater than or equal relations to show.
     * @param upperLimits an array of less than or equal relations to show.
     * @param aVector     an array of coefficients a for the affine linear relation proofs.
     * @param bVector     an array of summands a for the affine linear relation proofs.
     * @param hasWitness  determines whether a witness for this relation is known
     */
    public TokenUpdateLeaf(String leafName, Vector<BigInteger> lowerLimits, Vector<BigInteger> upperLimits, Vector<BigInteger> aVector, Vector<BigInteger> bVector, boolean hasWitness) {
        super(leafName);
        this.lowerLimits = lowerLimits;
        this.upperLimits = upperLimits;
        this.aVector = aVector;
        this.bVector = bVector;
        this.setHasWitness(hasWitness);
    }
}
