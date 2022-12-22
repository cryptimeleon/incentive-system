package org.cryptimeleon.incentive.crypto.proof.wellformedness;

import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.math.structures.groups.GroupElement;

import java.util.Objects;

public class CommitmentWellformednessCommonInput implements CommonInput {
    private final GroupElement c0Pre; // left part of preliminary token's commitment

    private final GroupElement c1Pre; // right part of the preliminary token's commitment

    private final GroupElement blindedUpk;

    private final GroupElement blindedW;

    public CommitmentWellformednessCommonInput(GroupElement c0Pre, GroupElement c1Pre, GroupElement blindedUpk, GroupElement blindedW) {
        this.c0Pre = c0Pre;
        this.c1Pre = c1Pre;
        this.blindedUpk = blindedUpk;
        this.blindedW = blindedW;
    }

    public GroupElement getC0Pre() {
        return this.c0Pre;
    }

    public GroupElement getC1Pre() {
        return this.c1Pre;
    }

    public GroupElement getBlindedUpk() {
        return this.blindedUpk;
    }

    public GroupElement getBlindedW() {
        return this.blindedW;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitmentWellformednessCommonInput that = (CommitmentWellformednessCommonInput) o;
        return Objects.equals(c0Pre, that.c0Pre) && Objects.equals(c1Pre, that.c1Pre) && Objects.equals(blindedUpk, that.blindedUpk) && Objects.equals(blindedW, that.blindedW);
    }

    @Override
    public int hashCode() {
        return Objects.hash(c0Pre, c1Pre, blindedUpk, blindedW);
    }

    public String toString() {
        return "CommitmentWellformednessCommonInput(c0Pre=" + this.getC0Pre() + ", c1Pre=" + this.getC1Pre() + ", blindedUpk=" + this.getBlindedUpk() + ", blindedW=" + this.getBlindedW() + ")";
    }
}
