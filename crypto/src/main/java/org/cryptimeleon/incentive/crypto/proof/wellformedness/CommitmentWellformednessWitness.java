package org.cryptimeleon.incentive.crypto.proof.wellformedness;

import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

import java.util.Objects;

/**
 * Objects of this class represent witnesses used for proving the well-formedness of commitments.
 */
public class CommitmentWellformednessWitness implements SecretInput {
    private final ZnElement usk; // user secret key
    private final ZnElement eskUsr;
    private final ZnElement dsrnd0;
    private final ZnElement dsrnd1;
    private final ZnElement z;
    private final ZnElement t;
    private final ZnElement uInverse;

    public CommitmentWellformednessWitness(ZnElement usk, ZnElement eskUsr, ZnElement dsrnd0, ZnElement dsrnd1, ZnElement z, ZnElement t, ZnElement uInverse) {
        this.usk = usk;
        this.eskUsr = eskUsr;
        this.dsrnd0 = dsrnd0;
        this.dsrnd1 = dsrnd1;
        this.z = z;
        this.t = t;
        this.uInverse = uInverse;
    }

    public ZnElement getUsk() {
        return this.usk;
    }

    public ZnElement getEskUsr() {
        return this.eskUsr;
    }

    public ZnElement getDsrnd0() {
        return this.dsrnd0;
    }

    public ZnElement getDsrnd1() {
        return this.dsrnd1;
    }

    public ZnElement getZ() {
        return this.z;
    }

    public ZnElement getT() {
        return this.t;
    }

    public ZnElement getUInverse() {
        return this.uInverse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitmentWellformednessWitness that = (CommitmentWellformednessWitness) o;
        return Objects.equals(usk, that.usk) && Objects.equals(eskUsr, that.eskUsr) && Objects.equals(dsrnd0, that.dsrnd0) && Objects.equals(dsrnd1, that.dsrnd1) && Objects.equals(z, that.z) && Objects.equals(t, that.t) && Objects.equals(uInverse, that.uInverse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usk, eskUsr, dsrnd0, dsrnd1, z, t, uInverse);
    }

    public String toString() {
        return "CommitmentWellformednessWitness(usk=" + this.getUsk() + ", eskUsr=" + this.getEskUsr() + ", dsrnd0=" + this.getDsrnd0() + ", dsrnd1=" + this.getDsrnd1() + ", z=" + this.getZ() + ", t=" + this.getT() + ", uInverse=" + this.getUInverse() + ")";
    }
}
