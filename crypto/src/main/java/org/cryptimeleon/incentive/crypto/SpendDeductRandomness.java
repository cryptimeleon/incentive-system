package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Data class for user randomness used in spend-deduct protocol.
 */
public class SpendDeductRandomness {
    final Zn.ZnElement eskUsrS;
    final Zn.ZnElement dsrnd0S;
    final Zn.ZnElement dsrnd1S;
    final Zn.ZnElement zS;
    final Zn.ZnElement tS;
    final Zn.ZnElement uS;

    public SpendDeductRandomness(Zn.ZnElement eskUsrS, Zn.ZnElement dsrnd0S, Zn.ZnElement dsrnd1S, Zn.ZnElement zS, Zn.ZnElement tS, Zn.ZnElement uS) {
        this.eskUsrS = eskUsrS;
        this.dsrnd0S = dsrnd0S;
        this.dsrnd1S = dsrnd1S;
        this.zS = zS;
        this.tS = tS;
        this.uS = uS;
    }
}
