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
    public final Zn.ZnElement esk;
    public final Zn.ZnElement eskStarUser;
    public final Zn.ZnElement dsrnd0;
    public final Zn.ZnElement dsrndStar0;
    public final Zn.ZnElement dsrnd1;
    public final Zn.ZnElement dsrndStar1;
    public final RingElementVector eskStarUserDec;
    public final RingElementVector rVector;
    public final RingElementVector pointsVector;
    public final RingElementVector newPointsVector;

    public SpendDeductZkpWitnessInput(Zn.ZnElement usk, Zn.ZnElement z, Zn.ZnElement zStar, Zn.ZnElement t, Zn.ZnElement tStar, Zn.ZnElement uStar, Zn.ZnElement esk, Zn.ZnElement eskStarUser, Zn.ZnElement dsrnd0, Zn.ZnElement dsrndStar0, Zn.ZnElement dsrnd1, Zn.ZnElement dsrndStar1, RingElementVector eskStarUserDec, RingElementVector rVector, RingElementVector pointsVector, RingElementVector newPointsVector) {
        this.usk = usk;
        this.z = z;
        this.zStar = zStar;
        this.t = t;
        this.tStar = tStar;
        this.uStar = uStar;
        this.esk = esk;
        this.eskStarUser = eskStarUser;
        this.dsrnd0 = dsrnd0;
        this.dsrndStar0 = dsrndStar0;
        this.dsrnd1 = dsrnd1;
        this.dsrndStar1 = dsrndStar1;
        this.eskStarUserDec = eskStarUserDec;
        this.rVector = rVector;
        this.pointsVector = pointsVector;
        this.newPointsVector = newPointsVector;
    }
}
