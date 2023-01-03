package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;

import java.util.Objects;

/**
 * Data class for the request sent in the credit-earn protocol.
 */
public class EarnRequest implements Representable {
    @Represented(restorer = "SPSEQ")
    private SPSEQSignature blindedSignature;

    @Represented(restorer = "G1")
    private GroupElement c0; // first element of the tuple C

    @Represented(restorer = "G1")
    private GroupElement c1; // second element of the tuple C

    public EarnRequest(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this)
                .register(pp.getBg().getG1(), "G1")
                .register(pp.getSpsEq(), "SPSEQ")
                .deserialize(repr);
    }

    public EarnRequest(SPSEQSignature blindedSignature, GroupElement c0, GroupElement c1) {
        this.blindedSignature = blindedSignature;
        this.c0 = c0;
        this.c1 = c1;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public SPSEQSignature getBlindedSignature() {
        return this.blindedSignature;
    }

    public GroupElement getC0() {
        return this.c0;
    }

    public GroupElement getC1() {
        return this.c1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EarnRequest that = (EarnRequest) o;
        return Objects.equals(blindedSignature, that.blindedSignature) && Objects.equals(c0, that.c0) && Objects.equals(c1, that.c1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blindedSignature, c0, c1);
    }

    public String toString() {
        return "EarnRequest(blindedSignature=" + this.getBlindedSignature() + ", c0=" + this.getC0() + ", c1=" + this.getC1() + ")";
    }
}
