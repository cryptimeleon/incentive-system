package de.upb.crypto.incentive.cryptoprotocol.model.keys.provider;

import de.upb.crypto.craco.prf.PrfKey;
import de.upb.crypto.craco.sig.sps.eq.SPSEQSigningKey;
import de.upb.crypto.math.structures.cartesian.RingElementVector;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

import java.util.Arrays;

public class ProviderSecretKey
{
    private SPSEQSigningKey skSpsEq;
    private RingElementVector q; // dlogs of the first six bases used in the Pedersen commitment in the token
    private PrfKey betaProv;

    public ProviderSecretKey(SPSEQSigningKey skSpsEq, RingElementVector q, PrfKey betaProv) throws IllegalArgumentException
    {
        // assert that correct number of Zn exponents is passed ()
        if(q.length() != 6)
        {
            throw new IllegalArgumentException("q is required to consist of 6 group elements, found: " + String.valueOf(q.length()));
        }

        this.skSpsEq = skSpsEq;

        // deep copy just to be sure
        this.q = q;

        this.betaProv = betaProv;
    }

    public SPSEQSigningKey getSkSpsEq() { return skSpsEq; }

    public RingElementVector getQ() { return this.q; }
}
