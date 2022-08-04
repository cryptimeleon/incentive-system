package org.cryptimeleon.incentive.crypto.model.messages;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessCommonInput;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElement;

/**
 * A class representing the first two messages of the Issue  {@literal <}-{@literal >} Join protocol.
 */
@Value
@AllArgsConstructor
public class JoinRequest implements Representable {
    @NonFinal
    GroupElement preCommitment0;

    @NonFinal
    GroupElement preCommitment1;

    @NonFinal
    FiatShamirProof cwfProof; // proof for well-formedness of token and knowledge of usk corresp. to upk

    // (upb^r, w^r) + genesisSignature^r (valid signature on this tuple)
    @NonFinal
    GroupElement blindedUpk;

    @NonFinal
    GroupElement blindedW;

    @NonFinal
    SPSEQSignature blindedGenesisSignature;


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
}
