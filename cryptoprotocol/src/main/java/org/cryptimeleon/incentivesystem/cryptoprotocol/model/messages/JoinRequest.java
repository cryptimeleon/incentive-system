package org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs.CommitmentWellformednessCommonInput;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElement;

/**
 * A class representing the first two messages of the Issue <-> Join protocol.
 */
@Value
@AllArgsConstructor
public class JoinRequest implements Representable {
    @NonFinal
    private GroupElement preCommitment0;

    @NonFinal
    private GroupElement preCommitment1;

    @NonFinal
    private FiatShamirProof cwfProof; // proof for well-formedness of token and knowledge of usk corresp. to upk

    public JoinRequest(Representation repr, IncentivePublicParameters pp, FiatShamirProofSystem fsps, CommitmentWellformednessCommonInput cwfProofCommonInput) {
        // force passed representation into a list representation (does not throw class cast exception in intended use cases)
        var list = (ListRepresentation) repr;

        // retrieve restorers
        var usedG1 = pp.getBg().getG1();

        // restore fields
        this.preCommitment0 = usedG1.restoreElement(list.get(0));
        this.preCommitment1 = usedG1.restoreElement(list.get(1));
        this.cwfProof = fsps.restoreProof(cwfProofCommonInput, list.get(2));
    }

    public Representation getRepresentation()
    {
        return new ListRepresentation(
                preCommitment0.getRepresentation(),
                preCommitment1.getRepresentation(),
                cwfProof.getRepresentation()
        );
    }
}
