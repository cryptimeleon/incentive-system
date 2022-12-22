package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.util.Objects;

/**
 * Provider's response to a spend-deduct request.
 */
public class SpendResponse implements Representable {
    @Represented(restorer = "SPSEQ")
    private SPSEQSignature sigma;

    @Represented(restorer = "Zn")
    private Zn.ZnElement eskProvStar;

    public SpendResponse(Representation repr, Zn zn, SPSEQSignatureScheme spseqSignatureScheme) {
        new ReprUtil(this).register(zn, "Zn").register(spseqSignatureScheme, "SPSEQ").deserialize(repr);
    }

    public SpendResponse(Representation repr, IncentivePublicParameters pp) {
        this(repr, pp.getBg().getZn(), pp.getSpsEq());
    }

    public SpendResponse(SPSEQSignature sigma, Zn.ZnElement eskProvStar) {
        this.sigma = sigma;
        this.eskProvStar = eskProvStar;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public SPSEQSignature getSigma() {
        return this.sigma;
    }

    public Zn.ZnElement getEskProvStar() {
        return this.eskProvStar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpendResponse that = (SpendResponse) o;
        return Objects.equals(sigma, that.sigma) && Objects.equals(eskProvStar, that.eskProvStar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sigma, eskProvStar);
    }

    public String toString() {
        return "SpendResponse(sigma=" + this.getSigma() + ", eskProvStar=" + this.getEskProvStar() + ")";
    }
}
