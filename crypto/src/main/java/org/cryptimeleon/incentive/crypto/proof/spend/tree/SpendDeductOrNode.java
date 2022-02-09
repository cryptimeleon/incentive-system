package org.cryptimeleon.incentive.crypto.proof.spend.tree;

import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;

public class SpendDeductOrNode implements SpendDeductTree {
    public final SpendDeductTree left, right;

    public SpendDeductOrNode(SpendDeductTree left, SpendDeductTree right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isValidForPoints(Vector<BigInteger> pointsVector, Vector<BigInteger> newPointsVector) {
        return left.isValidForPoints(pointsVector, newPointsVector) || right.isValidForPoints(pointsVector, newPointsVector);
    }
}
