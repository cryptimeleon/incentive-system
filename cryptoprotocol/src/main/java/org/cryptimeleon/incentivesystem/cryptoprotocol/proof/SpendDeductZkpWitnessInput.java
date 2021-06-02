package org.cryptimeleon.incentivesystem.cryptoprotocol.proof;

import lombok.AllArgsConstructor;
import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Witness for the spend-deduct ZKP
 */
@AllArgsConstructor
public class SpendDeductZkpWitnessInput implements SecretInput {
    public final Zn.ZnElement usk;
    public final Zn.ZnElement v;
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
}
