package org.cryptimeleon.incentive.crypto.proof.spend.tree;

public class SpendDeductAndNode implements SpendDeductTree {

    public final SpendDeductTree left, right;

    public SpendDeductAndNode(SpendDeductTree left, SpendDeductTree right) {
        this.left = left;
        this.right = right;
    }
}
