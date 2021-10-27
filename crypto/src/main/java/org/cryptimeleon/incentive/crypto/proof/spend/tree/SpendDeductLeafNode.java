package org.cryptimeleon.incentive.crypto.proof.spend.tree;

public abstract class SpendDeductLeafNode extends SpendDeductTree {

    // Indicate whether prover knows a witness for this statement
    private Boolean hasWitness = null;
    // Name must be unique
    private String leafName;

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
