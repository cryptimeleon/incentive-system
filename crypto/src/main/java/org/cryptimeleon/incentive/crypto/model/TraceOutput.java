package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

import java.util.Objects;

/**
 * Simple data class that represents outputs of the trace algorithm from the Cryptimeleon incentive system paper
 * when called on the data of some transaction T.
 */
public class TraceOutput {
    /**
     * Remainder token of T
     */
    private final GroupElement dsidStar;

    /**
     * ElGamal secret key associated to the remainder token of T,
     * i.e. the corresponding secret key associated to the double-spending ID (= ElGamal public key)
     * that is consumed by a transaction T' that follows T.
     */
    private final ZnElement dsTraceStar;

    public TraceOutput(GroupElement dsidStar, ZnElement dsTraceStar) {
        this.dsidStar = dsidStar;
        this.dsTraceStar = dsTraceStar;
    }

    public GroupElement getDsidStar() {
        return this.dsidStar;
    }

    public ZnElement getDsTraceStar() {
        return this.dsTraceStar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TraceOutput that = (TraceOutput) o;
        return Objects.equals(dsidStar, that.dsidStar) && Objects.equals(dsTraceStar, that.dsTraceStar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dsidStar, dsTraceStar);
    }

    public String toString() {
        return "TraceOutput(dsidStar=" + this.getDsidStar() + ", dsTraceStar=" + this.getDsTraceStar() + ")";
    }
}
