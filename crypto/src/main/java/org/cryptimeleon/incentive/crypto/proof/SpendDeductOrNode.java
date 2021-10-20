package org.cryptimeleon.incentive.crypto.proof;

public abstract class SpendDeductOrNode extends SpendDeductTree {

    abstract SpendDeductTree getLeft();

    abstract SpendDeductTree getRight();
}
