package org.cryptimeleon.incentive.crypto;

import lombok.AllArgsConstructor;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Data class for user randomness used in spend-deduct protocol.
 */
@AllArgsConstructor
public class SpendDeductRandomness {
    Zn.ZnElement eskUsrS;
    Zn.ZnElement dsrnd0S;
    Zn.ZnElement dsrnd1S;
    Zn.ZnElement zS;
    Zn.ZnElement tS;
    Zn.ZnElement uS;
}
