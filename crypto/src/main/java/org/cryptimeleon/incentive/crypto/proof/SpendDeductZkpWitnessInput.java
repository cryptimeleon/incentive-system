package org.cryptimeleon.incentive.crypto.proof;

import lombok.AllArgsConstructor;
import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Witness for the spend-deduct ZKP
 */
@AllArgsConstructor
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


    public SpendDeductZkpWitnessInput(Token token,
                                      Zn.ZnElement usk,
                                      Zn.ZnElement zStar,
                                      Zn.ZnElement tStar,
                                      Zn.ZnElement uStar,
                                      Zn.ZnElement eskStarUser,
                                      Zn.ZnElement dsrndStar0,
                                      Zn.ZnElement dsrndStar1,
                                      RingElementVector eskStarUserDec,
                                      RingElementVector rVector) {
        this.z = token.getZ();
        this.t = token.getT();
        this.esk = token.getEncryptionSecretKey();
        this.dsrnd0 = token.getDoubleSpendRandomness0();
        this.dsrnd1 = token.getDoubleSpendRandomness1();
        this.pointsVector = token.getPoints();
        this.usk = usk;
        this.zStar = zStar;
        this.tStar = tStar;
        this.uStar = uStar;
        this.eskStarUser = eskStarUser;
        this.dsrndStar0 = dsrndStar0;
        this.dsrndStar1 = dsrndStar1;
        this.eskStarUserDec = eskStarUserDec;
        this.rVector = rVector;
    }
}
