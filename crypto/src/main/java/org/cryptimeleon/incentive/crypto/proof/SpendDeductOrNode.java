package org.cryptimeleon.incentive.crypto.proof;

public class SpendDeductOrNode extends SpendDeductBinaryNode {
    SpendDeductOrNode(SpendDeductTree left, SpendDeductTree right) {
        this.left = left;
        this.right = right;
    }
}
