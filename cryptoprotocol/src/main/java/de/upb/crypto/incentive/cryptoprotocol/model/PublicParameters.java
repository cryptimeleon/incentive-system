package de.upb.crypto.incentive.cryptoprotocol.model;


import de.upb.crypto.craco.prf.aes.AesPseudorandomFunction;
import de.upb.crypto.craco.sig.sps.eq.SPSEQSignatureScheme;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.BilinearGroup;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * a class representing the public parameters of the 2020 incentive system
 */

@Data
@AllArgsConstructor
public class PublicParameters
{
    private BilinearGroup bg;
    private GroupElement w;
    private GroupElement h7;
    private AesPseudorandomFunction prf; // not in paper, but we need to store PRF that is used in incentive system instance somewhere
    private SPSEQSignatureScheme spsEq; // same here for SPS-EQ scheme
}
