package org.cryptimeleon.incentive.crypto.proof;

public class SpendDeductAndNode extends SpendDeductBinaryNode {

    SpendDeductAndNode(SpendDeductTree left, SpendDeductTree right) {
        this.left = left;
        this.right = right;
    }
}
