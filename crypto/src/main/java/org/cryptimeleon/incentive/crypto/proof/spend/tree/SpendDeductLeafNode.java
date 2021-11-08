package org.cryptimeleon.incentive.crypto.proof.spend.tree;

import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;

/**
 * Leaf nodes that represent the actual statements of the boolean formula.
 */
public abstract class SpendDeductLeafNode implements SpendDeductTree {

    // Name must be unique within the whole SpendDeductTree
    private final String leafName;

    /**
     * Create a leaf node with a name that is unique within the tree.
     *
     * @param leafName name of the leaf node
     */
    public SpendDeductLeafNode(String leafName) {
        this.leafName = leafName;
    }

    public String getLeafName() {
        return leafName;
    }

    /**
     * Function that determines whether this leaf node's ZKP is satisfied by the given old and new points vectors.
     *
     * @param pointsVector    vector containing the old token's points
     * @param newPointsVector vector containing the points the users want for their new token
     * @return whether the point vectors satisfy the ZKP relation
     */
    public abstract boolean isValidForPoints(RingElementVector pointsVector, RingElementVector newPointsVector);
}
