package org.cryptimeleon.incentive.crypto.proof;

public abstract class SpendDeductAndNode extends SpendDeductTree {

    abstract SpendDeductTree getLeft();

    abstract SpendDeductTree getRight();

}
