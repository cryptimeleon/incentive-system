package org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.math.structures.groups.GroupElement;

@Value
@AllArgsConstructor
public class CommitmentWellformednessCommonInput implements CommonInput {
    private final GroupElement upk; // user public key

    private final GroupElement c0Pre; // left part of preliminary token's commitment

    private final GroupElement c1Pre; // right part of the preliminary token's commitment
}
