package org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs.CommitmentWellformednessCommonInput;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;

/**
 * A class representing the first two messages of the Issue <-> Join protocol.
 */
@Value
@AllArgsConstructor
public class JoinRequest implements Representable {
    @NonFinal
    @Represented(restorer = "G1")
    private GroupElement preCommitment0;

    @NonFinal
    @Represented(restorer = "G1")
    private GroupElement preCommitment1;

    @NonFinal
    @Represented
    private FiatShamirProof cwfProof; // proof for well-formedness of token and knowledge of usk corresp. to upk

    @NonFinal
    @Represented
    private CommitmentWellformednessCommonInput cwfProofCommonInput; // common input of proof for well-formedness of token and knowledge of usk corresp. to upk

    public JoinRequest(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this)
                .register(pp.getBg().getZn(), "Zn")
                .register(pp.getBg().getG1(), "G1")
                .deserialize(repr);
    }

    public Representation getRepresentation()
    {
        return ReprUtil.serialize(this);
    }
}
