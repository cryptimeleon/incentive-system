package org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;

@Value
@AllArgsConstructor
public class CommitmentWellformednessCommonInput implements CommonInput, Representable {
    @NonFinal
    @Represented(restorer = "G1")
    private GroupElement upk; // user public key

    @NonFinal
    @Represented(restorer = "G1")
    private GroupElement c0Pre; // left part of preliminary token's commitment

    @NonFinal
    @Represented(restorer = "G1")
    private GroupElement c1Pre; // right part of the preliminary token's commitment

    public CommitmentWellformednessCommonInput(Representation repr, IncentivePublicParameters pp)
    {
        new ReprUtil(this)
                .register(pp.getBg().getG1(), "G1")
                .deserialize(repr);
    }

    public Representation getRepresentation() { return ReprUtil.serialize(this); }
}
