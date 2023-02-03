package org.cryptimeleon.incentive.crypto.proof.spend.zkp;

import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Witness for the spend-deduct ZKP
 */
public class SpendDeductZkpWitnessInput implements SecretInput {
    public final Zn.ZnElement usk;
    public final Zn.ZnElement z;
    public final Zn.ZnElement zStar;
    public final Zn.ZnElement t;
    public final Zn.ZnElement tStar;
    public final Zn.ZnElement uStar;
    public final Zn.ZnElement dsidUserStar;
    public final Zn.ZnElement dsrnd;
    public final Zn.ZnElement dsrndStar;
    public final RingElementVector pointsVector;
    public final RingElementVector newPointsVector;

    public SpendDeductZkpWitnessInput(Zn.ZnElement usk, Zn.ZnElement z, Zn.ZnElement zStar, Zn.ZnElement t, Zn.ZnElement tStar, Zn.ZnElement uStar, Zn.ZnElement dsidUserStar, Zn.ZnElement dsrnd, Zn.ZnElement dsrndStar0, RingElementVector pointsVector, RingElementVector newPointsVector) {
        this.usk = usk;
        this.z = z;
        this.zStar = zStar;
        this.t = t;
        this.tStar = tStar;
        this.uStar = uStar;
        this.dsidUserStar = dsidUserStar;
        this.dsrnd = dsrnd;
        this.dsrndStar = dsrndStar0;
        this.pointsVector = pointsVector;
        this.newPointsVector = newPointsVector;
    }
}
