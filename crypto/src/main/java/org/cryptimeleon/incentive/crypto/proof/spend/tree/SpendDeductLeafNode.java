package org.cryptimeleon.incentive.crypto.proof.spend.tree;

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
}
