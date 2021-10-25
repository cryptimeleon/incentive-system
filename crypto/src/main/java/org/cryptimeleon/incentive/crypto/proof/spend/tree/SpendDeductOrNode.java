package org.cryptimeleon.incentive.crypto.proof.spend.tree;

public class SpendDeductOrNode extends SpendDeductBinaryNode {
    public SpendDeductOrNode(SpendDeductTree left, SpendDeductTree right) {
        this.left = left;
        this.right = right;
    }
}
