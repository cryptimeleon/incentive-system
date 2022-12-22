package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.util.Objects;

/**
 * Simple data class representing the identifying information of a transaction.
 * This information is sent when querying the double-spending protection service for the containment of a specific transaction.
 */
public class TransactionIdentifier implements Representable {
    @Represented(restorer = "zn")
    private Zn.ZnElement tid;

    @Represented(restorer = "zn")
    private Zn.ZnElement gamma;

    public TransactionIdentifier(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this).register(pp.getBg().getZn(), "zn").deserialize(repr);
    }

    public TransactionIdentifier(Zn.ZnElement tid, Zn.ZnElement gamma) {
        this.tid = tid;
        this.gamma = gamma;
    }

    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public Zn.ZnElement getTid() {
        return this.tid;
    }

    public Zn.ZnElement getGamma() {
        return this.gamma;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionIdentifier that = (TransactionIdentifier) o;
        return Objects.equals(tid, that.tid) && Objects.equals(gamma, that.gamma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tid, gamma);
    }

    public String toString() {
        return "TransactionIdentifier(tid=" + this.getTid() + ", gamma=" + this.getGamma() + ")";
    }
}
