package org.cryptimeleon.incentive.crypto.proof.wellformedness;

import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

import java.util.Objects;

/**
 * Objects of this class represent witnesses used for proving the well-formedness of commitments.
 */
public class CommitmentWellformednessWitness implements SecretInput {
    private final ZnElement usk; // user secret key
    private final ZnElement dsidUser;
    private final ZnElement dsrnd;
    private final ZnElement z;
    private final ZnElement t;
    private final ZnElement uInverse;

    public CommitmentWellformednessWitness(ZnElement usk, ZnElement dsidUser, ZnElement dsrnd, ZnElement z, ZnElement t, ZnElement uInverse) {
        this.usk = usk;
        this.dsidUser = dsidUser;
        this.dsrnd = dsrnd;
        this.z = z;
        this.t = t;
        this.uInverse = uInverse;
    }

    public ZnElement getUsk() {
        return this.usk;
    }

    public ZnElement getDsidUser() {
        return dsidUser;
    }

    public ZnElement getDsrnd() {
        return this.dsrnd;
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
        return Objects.equals(usk, that.usk) && Objects.equals(dsidUser, that.dsidUser) && Objects.equals(dsrnd, that.dsrnd) && Objects.equals(z, that.z) && Objects.equals(t, that.t) && Objects.equals(uInverse, that.uInverse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usk, dsidUser, dsrnd, z, t, uInverse);
    }
}
