package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessCommonInput;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElement;

import java.util.Objects;

/**
 * A class representing the first two messages of the Issue  {@literal <}-{@literal >} Join protocol.
 */
public class JoinRequest implements Representable {
    private final GroupElement preCommitment0;

    private final GroupElement preCommitment1;

    private final FiatShamirProof cwfProof; // proof for well-formedness of token and knowledge of usk corresp. to upk

    // (upb^r, w^r) + genesisSignature^r (valid signature on this tuple)
    private final GroupElement blindedUpk;

    private final GroupElement blindedW;

    private final SPSEQSignature blindedGenesisSignature;


    public JoinRequest(Representation repr, IncentivePublicParameters pp, FiatShamirProofSystem fsps) {
        // force passed representation into a list representation (does not throw class cast exception in intended use cases)
        var list = (ListRepresentation) repr;

        // retrieve restorers
        var usedG1 = pp.getBg().getG1();

        // restore fields
        this.preCommitment0 = usedG1.restoreElement(list.get(0));
        this.preCommitment1 = usedG1.restoreElement(list.get(1));
        this.blindedUpk = usedG1.restoreElement(list.get(2));
        this.blindedW = usedG1.restoreElement(list.get(3));
        this.blindedGenesisSignature = pp.getSpsEq().restoreSignature(list.get(4));
        var cwfProofCommonInput = new CommitmentWellformednessCommonInput(preCommitment0, preCommitment1, blindedUpk, blindedW); // recompute cwf proof common input
        this.cwfProof = fsps.restoreProof(cwfProofCommonInput, list.get(5));
    }

    public JoinRequest(GroupElement preCommitment0, GroupElement preCommitment1, FiatShamirProof cwfProof, GroupElement blindedUpk, GroupElement blindedW, SPSEQSignature blindedGenesisSignature) {
        this.preCommitment0 = preCommitment0;
        this.preCommitment1 = preCommitment1;
        this.cwfProof = cwfProof;
        this.blindedUpk = blindedUpk;
        this.blindedW = blindedW;
        this.blindedGenesisSignature = blindedGenesisSignature;
    }

    public Representation getRepresentation() {
        return new ListRepresentation(
                this.preCommitment0.getRepresentation(),
                this.preCommitment1.getRepresentation(),
                this.blindedUpk.getRepresentation(),
                this.blindedW.getRepresentation(),
                this.blindedGenesisSignature.getRepresentation(),
                this.cwfProof.getRepresentation()
        );
    }

    public GroupElement getPreCommitment0() {
        return this.preCommitment0;
    }

    public GroupElement getPreCommitment1() {
        return this.preCommitment1;
    }

    public FiatShamirProof getCwfProof() {
        return this.cwfProof;
    }

    public GroupElement getBlindedUpk() {
        return this.blindedUpk;
    }

    public GroupElement getBlindedW() {
        return this.blindedW;
    }

    public SPSEQSignature getBlindedGenesisSignature() {
        return this.blindedGenesisSignature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoinRequest that = (JoinRequest) o;
        return Objects.equals(preCommitment0, that.preCommitment0) && Objects.equals(preCommitment1, that.preCommitment1) && Objects.equals(cwfProof, that.cwfProof) && Objects.equals(blindedUpk, that.blindedUpk) && Objects.equals(blindedW, that.blindedW) && Objects.equals(blindedGenesisSignature, that.blindedGenesisSignature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(preCommitment0, preCommitment1, cwfProof, blindedUpk, blindedW, blindedGenesisSignature);
    }

    public String toString() {
        return "JoinRequest(preCommitment0=" + this.getPreCommitment0() + ", preCommitment1=" + this.getPreCommitment1() + ", cwfProof=" + this.getCwfProof() + ", blindedUpk=" + this.getBlindedUpk() + ", blindedW=" + this.getBlindedW() + ", blindedGenesisSignature=" + this.getBlindedGenesisSignature() + ")";
    }
}
