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
public class CommitmentWellformednessCommonInput implements CommonInput {
    @NonFinal
    private GroupElement upk; // user public key

    @NonFinal
    private GroupElement c0Pre; // left part of preliminary token's commitment

    @NonFinal
    private GroupElement c1Pre; // right part of the preliminary token's commitment
}
