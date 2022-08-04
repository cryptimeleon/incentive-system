package org.cryptimeleon.incentive.crypto.proof.wellformedness;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.math.structures.groups.GroupElement;

@Value
@AllArgsConstructor
public class CommitmentWellformednessCommonInput implements CommonInput {
    @NonFinal
    GroupElement c0Pre; // left part of preliminary token's commitment

    @NonFinal
    GroupElement c1Pre; // right part of the preliminary token's commitment

    @NonFinal
    GroupElement blindedUpk;

    @NonFinal
    GroupElement blindedW;
}
