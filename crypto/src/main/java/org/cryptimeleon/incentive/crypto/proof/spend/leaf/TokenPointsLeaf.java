package org.cryptimeleon.incentive.crypto.proof.spend.leaf;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductLeafNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;

import java.math.BigInteger;

/**
 * Leaf node for representing range proofs over the points vector of the old token.
 * The statement has the form `for all i: lowerLimits[i] <= pointsVector[i] <= upperLimits[i]`
 * Also supports only partial range proofs by setting all other entries to null.
 */
public class TokenPointsLeaf extends SpendDeductLeafNode {
    public final BigInteger[] lowerLimits;
    public final BigInteger[] upperLimits;

    /**
     * Constructor with uninitialized hasWitness field, to be used by the verifier who neither knows nor needs that attribute.
     * Set members to null to indicate that no proof should be performed for that index.
     *
     * @param leafName    a name that uniquely identifies this leaf in a {@link SpendDeductTree}.
     * @param lowerLimits an array of greater than or equal relations to show.
     * @param upperLimits an array of less than or equal relations to show.
     */
    public TokenPointsLeaf(String leafName, BigInteger[] lowerLimits, BigInteger[] upperLimits) {
        super(leafName);
        this.lowerLimits = lowerLimits;
        this.upperLimits = upperLimits;
    }

    /**
     * Alternative constructor that can be used at the client's side to set the hasWitness field.
     * Set members to null to indicate that no proof should be performed for that index.
     *
     * @param leafName    a name that uniquely identifies this leaf in a {@link SpendDeductTree}.
     * @param lowerLimits an array of greater than or equal relations to show.
     * @param upperLimits an array of less than or equal relations to show.
     * @param hasWitness  determines whether a witness for this relation is known
     */
    public TokenPointsLeaf(String leafName, BigInteger[] lowerLimits, BigInteger[] upperLimits, boolean hasWitness) {
        super(leafName);
        this.lowerLimits = lowerLimits;
        this.upperLimits = upperLimits;
        this.setHasWitness(hasWitness);
    }
}
