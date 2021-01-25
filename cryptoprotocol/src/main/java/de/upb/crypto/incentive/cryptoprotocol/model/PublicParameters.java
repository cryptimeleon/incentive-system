package de.upb.crypto.incentive.cryptoprotocol.model;


import de.upb.crypto.craco.prf.aes.AesPseudorandomFunction;
import de.upb.crypto.craco.sig.interfaces.SignatureKeyPair;
import de.upb.crypto.craco.sig.sps.eq.SPSEQSignatureScheme;
import de.upb.crypto.craco.sig.sps.eq.SPSEQSigningKey;
import de.upb.crypto.craco.sig.sps.eq.SPSEQVerificationKey;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.BilinearGroup;

/**
 * a class representing the public parameters of the 2020 incentive system
 * @author Patrick Schürmann
 */

public class PublicParameters
{
    private BilinearGroup bg;
    private GroupElement w;
    private GroupElement h7;
    private AesPseudorandomFunction prf; // not in paper, but we need to store PRF that is used in incentive system instance somewhere
    private SPSEQSignatureScheme spsEq; // same here for SPS-EQ scheme
    private SignatureKeyPair<SPSEQVerificationKey, SPSEQSigningKey> spsEqKeyPair;

    public PublicParameters(BilinearGroup bg, GroupElement w, GroupElement h7, AesPseudorandomFunction prf, SPSEQSignatureScheme spsEq, SignatureKeyPair<SPSEQVerificationKey, SPSEQSigningKey> spsEqKeyPair)
    {
        this.bg = bg;
        this.w = w;
        this.h7 = h7;
        this.prf = prf;
        this.spsEq = spsEq;
        this.spsEqKeyPair = spsEqKeyPair;
    }

    public BilinearGroup getBG()
    {
        return this.bg;
    }

    public GroupElement getW()
    {
        return this.w;
    }

    public GroupElement getH7()
    {
        return this.h7;
    }

    public AesPseudorandomFunction getPrf() { return this.prf; }

    public SPSEQSignatureScheme getSpsEq() { return this.spsEq; }

    public SignatureKeyPair<SPSEQVerificationKey, SPSEQSigningKey> getSpsEqKeyPair() { return spsEqKeyPair; }
}
