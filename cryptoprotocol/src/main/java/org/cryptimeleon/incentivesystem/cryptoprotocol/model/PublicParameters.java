package org.cryptimeleon.incentivesystem.cryptoprotocol.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.cryptimeleon.craco.prf.aes.AesPseudorandomFunction;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;

/**
 * a class representing the public parameters of the 2020 incentive system
 */

@Data
@AllArgsConstructor
public class PublicParameters {
    private BilinearGroup bg;
    private GroupElement w;
    private GroupElement h7;
    private AesPseudorandomFunction prf; // not in paper, but we need to store PRF that is used in incentive system instance somewhere
    private SPSEQSignatureScheme spsEq; // same here for SPS-EQ scheme
}
