package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Data class for user randomness used in spend-deduct protocol.
 */
public class SpendDeductRandomness {
    final Zn.ZnElement dsidUserS;
    final Zn.ZnElement dsrndS;
    final Zn.ZnElement zS;
    final Zn.ZnElement tS;
    final Zn.ZnElement uS;

    public SpendDeductRandomness(Zn.ZnElement dsidUserS, Zn.ZnElement dsrndS, Zn.ZnElement zS, Zn.ZnElement tS, Zn.ZnElement uS) {
        this.dsidUserS = dsidUserS;
        this.dsrndS = dsrndS;
        this.zS = zS;
        this.tS = tS;
        this.uS = uS;
    }
}
