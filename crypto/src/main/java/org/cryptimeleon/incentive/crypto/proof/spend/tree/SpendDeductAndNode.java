package org.cryptimeleon.incentive.crypto.proof.spend.tree;

public class SpendDeductAndNode extends SpendDeductBinaryNode {

    public SpendDeductAndNode(SpendDeductTree left, SpendDeductTree right) {
        this.left = left;
        this.right = right;
    }
}
