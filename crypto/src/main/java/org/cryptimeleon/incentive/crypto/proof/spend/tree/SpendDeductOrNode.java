package org.cryptimeleon.incentive.crypto.proof.spend.tree;

import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;

public class SpendDeductOrNode implements SpendDeductTree {
    public final SpendDeductTree left, right;

    public SpendDeductOrNode(SpendDeductTree left, SpendDeductTree right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isValidForPoints(RingElementVector pointsVector, RingElementVector newPointsVector) {
        return left.isValidForPoints(pointsVector, newPointsVector) || right.isValidForPoints(pointsVector, newPointsVector);
    }
}
