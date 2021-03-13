package org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs.commitmentwellformedness;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.math.structures.groups.GroupElement;

@Value
@AllArgsConstructor
public class CommitmentWellformednessProofCommonInput implements CommonInput {
    // newly introduced for first fragment
    private final GroupElement upk; // user public key
    private final GroupElement w; // base for user public keys

    // newly introduced for second fragment
    private final GroupElement c0Pre; // left part of preliminary token's commitment
    private final GroupElement c0Prime; // c0Pre to power of 1/u^* (reason: avoid implementing double exponent proofs )

    // newly introduced for third fragment
    private final GroupElement h1, h2, h3, h4, h6, h7; // h5 not needed since it is risen to the power of 0 in initial token

    // newly introduced for fourth fragment
    private final GroupElement g1; // generator of G1
    private final GroupElement c1Pre; // right part of the preliminary token's commitment
}
