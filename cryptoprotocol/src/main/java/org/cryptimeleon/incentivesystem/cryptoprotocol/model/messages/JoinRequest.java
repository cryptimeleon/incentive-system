package org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.structures.groups.GroupElement;

@Value
@AllArgsConstructor
public class JoinRequest implements Representable {
    private GroupElement preCommitment0;
    private GroupElement preCommitment1;
    private FiatShamirProof commitmentWellFormednessProof; // proof for well-formedness of token and knowledge of usk corresp. to upk

    public Representation getRepresentation()
    {
        return ReprUtil.serialize(this);
    }
}
