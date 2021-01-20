package de.upb.crypto.incentive.cryptoprotocol.model.keys.provider;

import de.upb.crypto.craco.sig.sps.eq.SPSEQVerificationKey;
import de.upb.crypto.incentive.cryptoprotocol.exceptions.PedersenException;
import de.upb.crypto.math.interfaces.structures.GroupElement;

public class ProviderPublicKey
{
    SPSEQVerificationKey pkSpsEq;
    // TODO: regular signature verification key is missing
    GroupElement[] h; // first six bases for the Pedersen commitment in the tokens

    public ProviderPublicKey(SPSEQVerificationKey pkSpsEq, GroupElement[] h) throws PedersenException
    {
        // asserting correct number of group elements passed
        if(h.length != 6)
        {
            throw new PedersenException("h is required to consist of 6 group elements, found: " + String.valueOf(h.length));
        }

        this.pkSpsEq = pkSpsEq;

        // deep copy just to be sure
        for(int i=0; i< h.length; i++)
        {
            this.h[i] = h[i];
        }
    }

    public SPSEQVerificationKey getPkSpsEq() { return this.pkSpsEq; }

    /**
     * @return deep copy of the array h (bases used in the Pedersen commitment in a token)
     */
    public GroupElement[] getH()
    {
        GroupElement[] hCopy = new GroupElement[h.length];
        for (int i = 0; i < h.length; i++)
        {
            hCopy[i] = this.h[i];
        }
        return hCopy;
    }
}
