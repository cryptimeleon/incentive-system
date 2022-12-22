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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof CommitmentWellformednessCommonInput))
            return false;
        final CommitmentWellformednessCommonInput other = (CommitmentWellformednessCommonInput) o;
        final Object this$c0Pre = this.getC0Pre();
        final Object other$c0Pre = other.getC0Pre();
        if (!Objects.equals(this$c0Pre, other$c0Pre)) return false;
        final Object this$c1Pre = this.getC1Pre();
        final Object other$c1Pre = other.getC1Pre();
        if (!Objects.equals(this$c1Pre, other$c1Pre)) return false;
        final Object this$blindedUpk = this.getBlindedUpk();
        final Object other$blindedUpk = other.getBlindedUpk();
        if (!Objects.equals(this$blindedUpk, other$blindedUpk))
            return false;
        final Object this$blindedW = this.getBlindedW();
        final Object other$blindedW = other.getBlindedW();
        return Objects.equals(this$blindedW, other$blindedW);
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $c0Pre = this.getC0Pre();
        result = result * PRIME + ($c0Pre == null ? 43 : $c0Pre.hashCode());
        final Object $c1Pre = this.getC1Pre();
        result = result * PRIME + ($c1Pre == null ? 43 : $c1Pre.hashCode());
        final Object $blindedUpk = this.getBlindedUpk();
        result = result * PRIME + ($blindedUpk == null ? 43 : $blindedUpk.hashCode());
        final Object $blindedW = this.getBlindedW();
        result = result * PRIME + ($blindedW == null ? 43 : $blindedW.hashCode());
        return result;
    }

    public String toString() {
        return "CommitmentWellformednessCommonInput(c0Pre=" + this.getC0Pre() + ", c1Pre=" + this.getC1Pre() + ", blindedUpk=" + this.getBlindedUpk() + ", blindedW=" + this.getBlindedW() + ")";
    }
}
