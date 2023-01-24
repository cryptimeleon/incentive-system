package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;

import java.math.BigInteger;
import java.util.Objects;

public class EarnRequestECDSA {
    private final BigInteger promotionId;
    private final Vector<BigInteger> deltaK;
    private final EarnStoreCoupon earnStoreCoupon;
    private final SPSEQSignature spseqSignature;
    private final GroupElement cPrime0;
    private final GroupElement cPrime1;

    public EarnRequestECDSA(BigInteger promotionId, Vector<BigInteger> deltaK, EarnStoreCoupon earnStoreCoupon, SPSEQSignature spseqSignature, GroupElement cPrime0, GroupElement cPrime1) {
        this.promotionId = promotionId;
        this.deltaK = deltaK;
        this.earnStoreCoupon = earnStoreCoupon;
        this.spseqSignature = spseqSignature;
        this.cPrime0 = cPrime0;
        this.cPrime1 = cPrime1;
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    public Vector<BigInteger> getDeltaK() {
        return deltaK;
    }

    public EarnStoreCoupon getEarnStoreCoupon() {
        return earnStoreCoupon;
    }

    public SPSEQSignature getSpseqSignature() {
        return spseqSignature;
    }

    public GroupElement getcPrime0() {
        return cPrime0;
    }

    public GroupElement getcPrime1() {
        return cPrime1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EarnRequestECDSA that = (EarnRequestECDSA) o;
        return Objects.equals(promotionId, that.promotionId) && Objects.equals(deltaK, that.deltaK) && Objects.equals(earnStoreCoupon, that.earnStoreCoupon) && Objects.equals(spseqSignature, that.spseqSignature) && Objects.equals(cPrime0, that.cPrime0) && Objects.equals(cPrime1, that.cPrime1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promotionId, deltaK, earnStoreCoupon, spseqSignature, cPrime0, cPrime1);
    }
}
