package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.math.serialization.ByteArrayRepresentation;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;

import java.math.BigInteger;
import java.util.Objects;
import java.util.stream.Collectors;

public class EarnRequestECDSA implements Representable {
    @Represented
    private final BigInteger promotionId;
    @Represented
    private final Vector<BigInteger> deltaK;
    @Represented
    private final EarnStoreCouponSignature earnStoreCouponSignature;
    @Represented
    private final SPSEQSignature spseqSignature;
    @Represented(restorer = "bg")
    private final GroupElement cPrime0;
    @Represented(restorer = "bg")
    private final GroupElement cPrime1;

    public EarnRequestECDSA(BigInteger promotionId, Vector<BigInteger> deltaK, EarnStoreCouponSignature earnStoreCouponSignature, SPSEQSignature spseqSignature, GroupElement cPrime0, GroupElement cPrime1) {
        this.promotionId = promotionId;
        this.deltaK = deltaK;
        this.earnStoreCouponSignature = earnStoreCouponSignature;
        this.spseqSignature = spseqSignature;
        this.cPrime0 = cPrime0;
        this.cPrime1 = cPrime1;
    }

    public EarnRequestECDSA(Representation representation, IncentivePublicParameters pp) {
        ListRepresentation listRepresentation = (ListRepresentation) representation;
        this.promotionId = new BigInteger(((ByteArrayRepresentation) listRepresentation.get(0)).get());
        this.deltaK = new Vector<>(((ListRepresentation) listRepresentation.get(1)).stream().map(r -> new BigInteger(((ByteArrayRepresentation) r).get())).collect(Collectors.toList()));
        this.earnStoreCouponSignature = new EarnStoreCouponSignature(listRepresentation.get(2));
        this.spseqSignature = new SPSEQSignature(listRepresentation.get(3), pp.getBg().getG1(), pp.getBg().getG2());
        this.cPrime0 = pp.getBg().getG1().restoreElement(listRepresentation.get(4));
        this.cPrime1 = pp.getBg().getG1().restoreElement(listRepresentation.get(5));
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    public Vector<BigInteger> getDeltaK() {
        return deltaK;
    }

    public EarnStoreCouponSignature getEarnStoreCoupon() {
        return earnStoreCouponSignature;
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
        return Objects.equals(promotionId, that.promotionId) && Objects.equals(deltaK, that.deltaK) && Objects.equals(earnStoreCouponSignature, that.earnStoreCouponSignature) && Objects.equals(spseqSignature, that.spseqSignature) && Objects.equals(cPrime0, that.cPrime0) && Objects.equals(cPrime1, that.cPrime1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promotionId, deltaK, earnStoreCouponSignature, spseqSignature, cPrime0, cPrime1);
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(
                new ByteArrayRepresentation(promotionId.toByteArray()),
                new ListRepresentation(deltaK.map(bigInteger -> new ByteArrayRepresentation(bigInteger.toByteArray())).stream().collect(Collectors.toList())),
                earnStoreCouponSignature.getRepresentation(),
                spseqSignature.getRepresentation(),
                cPrime0.getRepresentation(),
                cPrime1.getRepresentation()
        );
    }
}