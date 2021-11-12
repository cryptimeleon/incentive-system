package org.cryptimeleon.incentive.crypto.proof.spend.tree;

import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;

public class SpendDeductAndNode implements SpendDeductTree {

    public final SpendDeductTree left, right;

    public SpendDeductAndNode(SpendDeductTree left, SpendDeductTree right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isValidForPoints(RingElementVector pointsVector, RingElementVector newPointsVector) {
        return left.isValidForPoints(pointsVector, newPointsVector) && right.isValidForPoints(pointsVector, newPointsVector);
    }
}
