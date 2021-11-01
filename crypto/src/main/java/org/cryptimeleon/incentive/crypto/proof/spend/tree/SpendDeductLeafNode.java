package org.cryptimeleon.incentive.crypto.proof.spend.tree;

/**
 * Leaf nodes that represent the actual statements of the boolean formula.
 */
public abstract class SpendDeductLeafNode implements SpendDeductTree {

    // Name must be unique within the whole SpendDeductTree
    private final String leafName;

    // Indicate whether prover knows a witness for this statement
    private Boolean hasWitness = null;

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
     * Getter for the hasWitness field. Throws a runtime exception if accessed even though hasWitness is not set.
     */
    public boolean hasWitness() {
        // Can be uninitialized on verifier's side
        if (hasWitness == null) {
            throw new RuntimeException("hasWitness is not initialized!");
        }
        return hasWitness;
    }

    public void setHasWitness(boolean hasWitness) {
        this.hasWitness = hasWitness;
    }
}
