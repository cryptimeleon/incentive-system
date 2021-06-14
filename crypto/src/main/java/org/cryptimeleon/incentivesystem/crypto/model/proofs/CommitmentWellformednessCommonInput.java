package org.cryptimeleon.incentivesystem.crypto.model.proofs;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.protocols.CommonInput;
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
