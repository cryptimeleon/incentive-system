package org.cryptimeleon.incentive.crypto.proof.spend.tree;

public class SpendDeductOrNode implements SpendDeductTree {
    public final SpendDeductTree left, right;

    public SpendDeductOrNode(SpendDeductTree left, SpendDeductTree right) {
        this.left = left;
        this.right = right;
    }
}
