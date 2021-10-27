package org.cryptimeleon.incentive.crypto.proof.spend.tree;

public abstract class SpendDeductLeafNode implements SpendDeductTree {

    // Name must be unique within the whole SpendDeductTree
    private final String leafName;

    // Indicate whether prover knows a witness for this statement
    private Boolean hasWitness = null;

    public SpendDeductLeafNode(String leafName) {
        this.leafName = leafName;
    }

    public String getLeafName() {
        return leafName;
    }

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
