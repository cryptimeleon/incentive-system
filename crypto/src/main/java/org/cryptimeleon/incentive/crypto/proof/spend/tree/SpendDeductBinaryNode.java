package org.cryptimeleon.incentive.crypto.proof.spend.tree;

public abstract class SpendDeductBinaryNode extends SpendDeductTree {
    protected SpendDeductTree left, right;

    public SpendDeductTree getLeft() {
        return left;
    }

    public SpendDeductTree getRight() {
        return right;
    }
}
